import re
import os
import random
import requests
import time
import redis
import logging
from datetime import datetime
from flask import Flask, json, jsonify, request, make_response, send_from_directory
from flask_caching import Cache
from statsig.statsig_user import StatsigUser
from statsig import statsig, StatsigOptions, StatsigEnvironmentTier
import dotenv
from .db import decrement_inventory, get_products, get_products_join, get_inventory, get_promo_code
from .utils import parseHeaders, get_iterator, evaluate_statsig_flags
from .queues.tasks import sendEmail
import sentry_sdk
from sentry_sdk.integrations.flask import FlaskIntegration
from sentry_sdk.integrations.sqlalchemy import SqlalchemyIntegration
from sentry_sdk.integrations.redis import RedisIntegration
from sentry_sdk.integrations.logging import LoggingIntegration
from sentry_sdk.integrations.statsig import StatsigIntegration
from celery import Celery, states
from celery.exceptions import Ignore

RUBY_CUSTOM_HEADERS = ['se', 'customerType', 'email']
pests = ["aphids", "thrips", "spider mites", "lead miners", "scale", "whiteflies", "earwigs", "cutworms", "mealybugs",
         "fungus gnats"]

RELEASE = None
DSN = None
ENVIRONMENT = None
BACKEND_URL_RUBYONRAILS = None
RUN_SLOW_PROFILE = None

NORMAL_SLOW_PROFILE = 2  # seconds
EXTREMELY_SLOW_PROFILE = 24

def before_send(event, hint):
    # 'se' tag may have been set in app.before_request
    se = None
    if 'tags' in event.keys() and 'se' in event['tags']:
        se = event['tags']['se']

    if se not in [None, "undefined"]:
        se_tda_prefix_regex = r"[^-]+-tda-[^-]+-"
        se_fingerprint = se
        prefix = re.findall(se_tda_prefix_regex, se)
        if prefix:
            # Now that TDA puts platform/browser and test path into SE tag we want to prevent
            # creating separate issues for those. See https://github.com/sentry-demos/empower/pull/332
            se_fingerprint = prefix[0]
        
        if se.startswith('prod-tda-'):
            event['fingerprint'] = ['{{ default }}', se_fingerprint, RELEASE]
        else:
            event['fingerprint'] = ['{{ default }}', se_fingerprint]

    return event

def traces_sampler(sampling_context):
    sentry_sdk.set_context("sampling_context", sampling_context)
    REQUEST_METHOD = sampling_context['wsgi_environ']['REQUEST_METHOD']
    if REQUEST_METHOD == 'OPTIONS':
        return 0.0
    else:
        return 1.0

class MyFlask(Flask):
    def __init__(self, import_name, *args, **kwargs):
        global RELEASE, DSN, ENVIRONMENT, BACKEND_URL_RUBYONRAILS, RUN_SLOW_PROFILE, redis_client, cache;
        dotenv.load_dotenv()

        RELEASE = os.environ["FLASK_RELEASE"]
        DSN = os.environ["FLASK_DSN"]
        ENVIRONMENT = os.environ["FLASK_ENVIRONMENT"]
        BACKEND_URL_RUBYONRAILS = os.environ["BACKEND_URL_RUBYONRAILS"]

        RUN_SLOW_PROFILE = True
        if "RUN_SLOW_PROFILE" in os.environ:
            RUN_SLOW_PROFILE = os.environ["RUN_SLOW_PROFILE"].lower() == "true"

        sentry_sdk.init(
            dsn=DSN,
            release=RELEASE,
            environment=ENVIRONMENT,
            enable_logs=True,
            integrations=[
                FlaskIntegration(),
                SqlalchemyIntegration(),
                RedisIntegration(cache_prefixes=["flask.", "ruby."]),
                StatsigIntegration(),
                LoggingIntegration(event_level=None)  # don't send ERROR level logs as events/errors
            ],
            traces_sample_rate=1.0,
            before_send=before_send,
            traces_sampler=traces_sampler,
            _experiments={
                "profiles_sample_rate": 1.0,
            }
        )

        statsig.initialize(os.environ["STATSIG_SERVER_KEY"])

        super(MyFlask, self).__init__(import_name, *args, **kwargs)

        redis_host = os.environ["FLASK_REDISHOST"]
        redis_port = int(os.environ["FLASK_REDISPORT"])

        cache_config = {
            "DEBUG": True,
            "CACHE_TYPE": "RedisCache",
            "CACHE_DEFAULT_TIMEOUT": 300,
            "CACHE_REDIS_HOST": redis_host,
            "CACHE_REDIS_PORT": redis_port,
            "CACHE_KEY_PREFIX": None
        }

        self.config.from_mapping(cache_config)
        cache = Cache(self)

        redis_client = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logger.info("Flask application initialized")
logger.info("DSN: %s", DSN)
logger.info("RELEASE: %s", RELEASE)
logger.info("ENVIRONMENT: %s", ENVIRONMENT)

# This ensures CORS headers are applied to ALL responses, including 500 errors
# upgrading flask-cors from 3.0.10 to 6.0.1 and flask from 3.0.0 to 3.1.1 alone did not fix the issue
# doesn't seem to be related to https://github.com/corydolphin/flask-cors/issues/210 as we don't set
# debug=True anywhere. However suspiciously it didn't show up in production/TDA only when testing
# locally against staging.
class CORSWSGIWrapper:
    def __init__(self, app):
        self.app = app

    def __call__(self, environ, start_response):
        def custom_start_response(status, headers, exc_info=None):
            headers.append(('Access-Control-Allow-Origin', '*'))
            headers.append(('Access-Control-Allow-Headers', '*'))  # needed for 'customertype' and other "tag headers"
            return start_response(status, headers, exc_info)

        try:
            return self.app(environ, custom_start_response)
        except Exception as e:
            pass
        # If an exception occurs, create a response with CORS headers
        status = '500 Internal Server Error'
        headers = [
            ('Content-Type', 'application/json'),
            ('Access-Control-Allow-Origin', '*'),
            ('Access-Control-Allow-Headers', '*'),
        ]
        response_body = json.dumps({"error": "Internal Server Error"}).encode('utf-8')

        def error_start_response(status, headers, exc_info=None):
            return start_response(status, headers, exc_info)

        error_start_response(status, headers)
        return [response_body]

    def __getattr__(self, name):
        # Delegate attribute access to the underlying Flask app e.g. app.config
        return getattr(self.app, name)

app = MyFlask(__name__)
app = CORSWSGIWrapper(app)

@app.route('/enqueue', methods=['POST'])
def enqueue():
    logger.info('Received /enqueue endpoint request')

    body = json.loads(request.data)
    email = body['email']
    r = sendEmail.apply_async(args=[email], queue='celery-new-subscriptions')

    logger.info('Completed /enqueue request - email task enqueued')

    return jsonify({"status": "success"}), 200

@app.route('/checkout', methods=['POST'])
def checkout():
    logger.info('Received /checkout endpoint request')

    try:
        evaluate_statsig_flags()
    except Exception as e:
        sentry_sdk.capture_exception(e)
        logger.error('Error evaluating Statsig flags')

    order = json.loads(request.data)
    cart = order["cart"]
    form = order["form"]
    validate_inventory = True if "validate_inventory" not in order else order["validate_inventory"] == "true"

    logger.info('Processing /checkout - validating order details')

    inventory = []
    try:
        inventory = get_inventory(cart)
    except Exception as err:
        logger.error('Failed to get inventory')
        raise (err)

    fulfilled_count = 0
    out_of_stock = []  # list of items that are out of stock
    try:
        if validate_inventory:
            with sentry_sdk.start_span(op="code.block", name="checkout.process_order"):
                quantities = {int(k): v for k, v in cart['quantities'].items()}
                if len(quantities) == 0:
                    raise Exception("Invalid checkout request: cart is empty")

                inventory_dict = {x.productid: x for x in inventory}
                for product_id in quantities:
                    inventory_count = inventory_dict[product_id].count if product_id in inventory_dict else 0
                    if inventory_count >= quantities[product_id]:
                        decrement_inventory(inventory_dict[product_id].id, quantities[product_id])
                        fulfilled_count += 1
                    else:
                        title = list(filter(lambda x: x['id'] == product_id, cart['items']))[0]['title']
                        out_of_stock.append(title)
    except Exception as err:

        logger.error('Failed to validate inventory with cart: %s', cart)
        raise Exception("Error validating enough inventory for product") from err

    if len(out_of_stock) == 0:
        sentry_sdk.metrics.distribution("checkout.captured.revenue", cart["total"], unit="none")
        result = {'status': 'success'}
        logging.info("Checkout successful")
    else:
        # react doesn't handle these yet, shows "Checkout complete" as long as it's HTTP 200
        if fulfilled_count == 0:
            result = {'status': 'failed'}  # All items are out of stock
        else:
            result = {'status': 'partial', 'out_of_stock': out_of_stock}

    return make_response(json.dumps(result))

@app.route('/success', methods=['GET'])
def success():
    logger.info('Received /success endpoint request')

    logger.info('Completed /success request')
    return "success from flask"

@app.route('/products', methods=['GET'])
def products():
    logger.info('Received /products endpoint request')

    cache_key = str(random.randrange(100))

    product_inventory = None
    fetch_promotions = request.args.get('fetch_promotions')
    in_stock_only = request.args.get('in_stock_only')
    timeout_seconds = (EXTREMELY_SLOW_PROFILE if fetch_promotions else NORMAL_SLOW_PROFILE)

    logger.info('Processing /products')

    # Adding 0.5 seconds to the ruby /api_request in order to show caching
    # However, we want to keep the total trace time the same to preserve web vitals (+ other) functionality in sentry
    # Cache hits should keep the current delay, while cache misses will move 0.5 over to the ruby span
    ruby_delay_time = 0
    if (cache_key != "7"):
        timeout_seconds -= 0.5
        ruby_delay_time = 0.5

    try:
        with sentry_sdk.start_span(op="code.block", name="products.get_and_process_products"):
            rows = get_products()

            if RUN_SLOW_PROFILE:
                start_time = time.time()
                productsJSON = json.loads(rows)
                descriptions = [product["description"] for product in productsJSON]
                # this is improper convention (op and name switched up)
                # keeping it to avoid breaking changes in the demo
                with sentry_sdk.start_span(op="/get_iterator", name="code.block"):
                    loop = get_iterator(len(descriptions) * 6 + (2 if fetch_promotions else -1))

                for i in range(loop * 10):
                    time_delta = time.time() - start_time
                    if time_delta > timeout_seconds:
                        break

                for i, description in enumerate(descriptions):
                    for pest in pests:
                        if in_stock_only and productsJSON[i] not in product_inventory:
                            continue
                        if pest in description:
                            try:
                                del productsJSON[i:i + 1]
                            except:
                                productsJSON = json.loads(rows)
    except Exception as err:
        logger.error('Processing /products - error occurred')
        sentry_sdk.capture_exception(err)
        raise (err)

    logger.info('Completed /products request')

    get_api_response_with_caching(cache_key, ruby_delay_time)

    return rows

@sentry_sdk.trace
def get_api_response_with_caching(key, delay):
    start_time = time.time()
    logger.info('Processing /products - starting API request')

    cached_response = redis_client.get("ruby.api.cache:" + str(key))

    if cached_response is not None:
        logger.info('Processing /products - cache hit for API request')

        return cached_response

    logger.info('Processing /products - cache miss for API request')

    try:
        with sentry_sdk.start_span(op="code.block", name="call_api_on_cache_miss"):
            headers = parseHeaders(RUBY_CUSTOM_HEADERS, request.headers)
            r = requests.get(BACKEND_URL_RUBYONRAILS + "/api", headers=headers)
            r.raise_for_status()  # returns an HTTPError object if an error has occurred during the process

            time_delta = time.time() - start_time
            sleep_time = delay - time_delta
            if sleep_time > 0:
                time.sleep(sleep_time)

            # For demo show we want to show cache misses so only save 1 / 100
            if key == 7:
                logger.info('Processing /products - caching API response')
                redis_client.set("ruby.api.cache:" + str(key), key)

    except Exception as err:
        logger.error('Processing /products - API request failed')
        sentry_sdk.capture_exception(err)

    return key

@app.route('/products-join', methods=['GET'])
def products_join():
    logger.info('Received /products-join endpoint request')

    try:
        rows = get_products_join()
        logger.info('Processing /products-join - data retrieved')
    except Exception as err:
        logger.error('Processing /products-join - error getting data')
        sentry_sdk.capture_exception(err)
        raise (err)

    try:
        headers = parseHeaders(RUBY_CUSTOM_HEADERS, request.headers)
        r = requests.get(BACKEND_URL_RUBYONRAILS + "/api", headers=headers)
        r.raise_for_status()  # returns an HTTPError object if an error has occurred during the process
        logger.info('Processing /products-join - backend API call successful')
    except Exception as err:
        logger.error('Processing /products-join - backend API call failed')
        sentry_sdk.capture_exception(err)

    return rows

@app.route('/handled', methods=['GET'])
def handled_exception():
    logger.info('Received /handled endpoint request')

    try:
        '2' + 2
    except Exception as err:
        logger.error('Processing /handled - intentional exception occurred')
        sentry_sdk.capture_exception(err)
    return 'failed'

@app.route('/unhandled', methods=['GET'])
def unhandled_exception():
    logger.info('Received /unhandled endpoint request')

    obj = {}
    obj['keyDoesnt Exist']

@app.route('/api', methods=['GET'])
def api():
    logger.info('Received /api endpoint request')
    return "flask /api"

@app.route('/organization', methods=['GET'])
@cache.cached(timeout=1000, key_prefix="flask.cache.organization")
def organization():
    logger.info('Received /organization endpoint request')

    # perform get_products db query 1% of time in order
    # to populate "Found In" endpoints in Queries
    if random.random() < 0.01:
        logger.info('Processing /organization - executing random products query')
        rows = get_products()
    return "flask /organization"

@app.route('/connect', methods=['GET'])
def connect():
    logger.info('Received /connect endpoint request')
    return "flask /connect"

@app.route('/apply-promo-code', methods=['POST'])
def apply_promo_code():
    logger.info('[/apply-promo-code] request received')

    try:
        body = json.loads(request.data)
        promo_code = body.get('value', '').strip()
        
        if not promo_code:
            logger.warning('[/apply-promo-code] bad request - missing value parameter')
            return '', 400
        
        promo_code_data = get_promo_code(promo_code)
        
        if not promo_code_data:
            logger.warning('[/apply-promo-code] code not found: %s', promo_code)
            return jsonify({
                "error": {
                    "code": "not_found",
                    "message": "Promo code not found."
                }
            }), 404
        
        promo_dict = dict(promo_code_data)
        logger.info('[/apply-promo-code] code found: %s', promo_dict)
        
        if promo_dict.get('expires_at') and promo_dict['expires_at'] <= datetime.now():
            logger.warning('[/apply-promo-code] code has expired: %s', promo_code)
            return jsonify({
                "error": {
                    "code": "expired",
                    "message": "Provided coupon code has expired."
                }
            }), 410  # Look what a clever HTTP response code! Good luck FE dev :D
        
        logger.info('[/apply-promo-code] valid code found: %s', promo_dict)
        
        return jsonify({
            "success": True,
            "promo_code": {
                "code": promo_dict['code'],
                "percent_discount": promo_dict['percent_discount'],
                "max_dollar_savings": promo_dict['max_dollar_savings']
            }
        }), 200
    
    except Exception as err:
        sentry_sdk.capture_exception(err)
        return '', 500

@app.route('/product/0/info', methods=['GET'])
def product_info():
    logger.info('Received /product/0/info endpoint request')

    time.sleep(.55)
    logger.info('Completed /product/0/info request')
    return "flask /product/0/info"

# uncompressed assets
@app.route('/uncompressed_assets/<path:path>')
def send_report(path):
    logger.info('Received /uncompressed_assets request')

    time.sleep(.55)
    response = send_from_directory('../uncompressed_assets', path)
    # `Timing-Allow-Origin: *` allows timing/sizes to visbile in span
    response.headers['Timing-Allow-Origin'] = '*'
    # Overwriting `Content-Type` header to disable compression
    response.headers['Content-Type'] = 'application/octet-stream'

    logger.info('Completed /uncompressed_assets request')

    return response

# compressed assets
@app.route('/compressed_assets/<path:path>')
def send_report_configured_properly(path):
    logger.info('Received /compressed_assets request')

    response = send_from_directory('../compressed_assets', path)
    # `Timing-Allow-Origin: *` allows timing/sizes to visbile in span
    response.headers['Timing-Allow-Origin'] = '*'

    logger.info('Completed /compressed_assets request')

    return response

@app.before_request
def sentry_event_context():
    # Extract context information
    se = request.headers.get('se')
    customerType = request.headers.get('customerType')
    email = request.headers.get('email')
    cexp = request.headers.get('cexp')

    # Log request context information
    logger.debug('Setting up request context')

    if se not in [None, "undefined"]:
        sentry_sdk.set_tag("se", se)
    else:
        # sometimes this is the only way to propagate, e.g. when requested through a dynamically
        # inserted HTML tag as in case with (un)compressed_assets
        se = request.args.get('se')
        if se not in [None, "undefined"]:
            sentry_sdk.set_tag("se", se)

    if customerType not in [None, "undefined"]:
        sentry_sdk.set_tag("customerType", customerType)

    if email not in [None, "undefined"]:
        sentry_sdk.set_user({"email": email})

    if cexp not in [None, "undefined"]:
        sentry_sdk.set_tag("cexp", cexp)
