from fastapi import FastAPI
from model_inference.inference import CF_inference_fast
from online_evaluation.online_eval import OnlineEvaluator
from prometheus_fastapi_instrumentator import Instrumentator, metrics
from prometheus_client import Gauge

inference_algo_name = 'SVD'

# Instantiating FastAPI
api = FastAPI()

instrumentator = Instrumentator(
    should_group_status_codes=False,
    should_ignore_untemplated=True,
    # should_respect_env_var=True,
    should_instrument_requests_inprogress=True,
    # excluded_handlers=[".*admin.*", "/metrics"],
    # env_var_name="ENABLE_METRICS",
    inprogress_name="inprogress",
    inprogress_labels=True,
)

instrumentator.add(metrics.latency(buckets=(1, 2, 3,)))


# Defining a test root path and message
@api.get('/')
def root():
    return {'message': 'Welcome to Favor8!'}


# Defining the recommendation endpoint
@api.get('/recommend/{userid}')
async def recommend(userid: int):
    ids = CF_inference_fast(inference_algo_name, userid)
    return ','.join(x for x in ids)


@api.get('/evaluate')
async def evaluate():
    print("evaluate endpoint reached")
    evaluator = OnlineEvaluator()
    return evaluator.evaluate()

def online_evaluation_metrics():
    COS_SIM = Gauge(
        "online_evaluation_cos_sim", 
        "Cosine Similarity between recommended movies and movies actually watched"
    )
    AVG_RESPONSE_TIME = Gauge(
        "online_evaluation_avg_response_time",
        "Average response time for recommendation requests to return a response back to client"
    )
    AVG_MINUTE_COUNT = Gauge(
        "online_evaluation_avg_minute_count",
        "Average number of minutes users watch recommended movies"
    )

    def instrumentation(info):
        evaluator = OnlineEvaluator()
        online_metrics = evaluator.evaluate()
        COS_SIM.set(online_metrics["cos_sim"])
        AVG_RESPONSE_TIME.set(online_metrics["avg_response_time"])
        AVG_MINUTE_COUNT.set(online_metrics["avg_minute_count"])
    return instrumentation

instrumentator.add(online_evaluation_metrics())
instrumentator.instrument(api)
instrumentator.expose(api, include_in_schema=True, should_gzip=True)

