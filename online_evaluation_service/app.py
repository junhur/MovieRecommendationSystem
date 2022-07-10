from fastapi import FastAPI
from prometheus_fastapi_instrumentator import Instrumentator
from fastapi_utils.tasks import repeat_every
from prometheus_client import Gauge
from online_evaluation.online_eval import OnlineEvaluator


app = FastAPI()

instrumentator = Instrumentator(
    should_group_status_codes=False,
    should_ignore_untemplated=True,
    should_instrument_requests_inprogress=True,
    inprogress_name="inprogress",
    inprogress_labels=True,
)

@app.get('/')
def root():
    return {"Welcome": "Online Evaluation Service"}

@app.on_event('startup')
@repeat_every(seconds=60*5)
def evaluate():
    evaluator = OnlineEvaluator()
    online_metrics = evaluator.evaluate()
    cos_sim = online_metrics["cos_sim"]
    avg_response_time = online_metrics["avg_response_time"]
    avg_minute_count = online_metrics["avg_minute_count"]
    if any([cos_sim == 0, avg_response_time == 0, avg_minute_count == 0]):
        return
    with open("metrics.txt", "w") as file:
        file.write(str(cos_sim)+"\n")
        file.write(str(avg_response_time)+"\n")
        file.write(str(avg_minute_count)+"\n")


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
        try:
            with open("metrics.txt", "r") as file:
                lines = file.readlines()
                COS_SIM.set(float(lines[0]))
                AVG_RESPONSE_TIME.set(float(lines[1]))
                AVG_MINUTE_COUNT.set(float(lines[2]))
        except:
            COS_SIM.set(0)
            AVG_RESPONSE_TIME.set(0)
            AVG_MINUTE_COUNT.set(0)
    return instrumentation

instrumentator.add(online_evaluation_metrics())
instrumentator.instrument(app)
instrumentator.expose(app, include_in_schema=True, should_gzip=True)