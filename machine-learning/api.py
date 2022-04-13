from fastapi import FastAPI
from fastapi_utils.tasks import repeat_every
from model_inference.inference import CF_inference_fast
from model_training.scheduler import retrain_model
from online_evaluation.online_eval import OnlineEvaluator

inference_algo_name_a = 'SVD'
inference_algo_name_b = 'SVD'

# Instantiating FastAPI
api = FastAPI()


# Defining a test root path and message
@api.get('/')
def root():
    return {'message': 'Welcome to Favor8!'}


# Defining the recommendation endpoints
# default endpoint and the testing endpoint A
@api.get('/recommend/a/{userid}')
@api.get('/recommend/{userid}')
async def recommend(userid: int):
    ids, version = CF_inference_fast(inference_algo_name_a, userid, True)
    return ','.join(x for x in ids), version


# testing endpoint B
@api.get('/recommend/b/{userid}')
async def recommend(userid: int):
    ids, version = CF_inference_fast(inference_algo_name_b, userid, False)
    return ','.join(x for x in ids), version


@api.get('/evaluate')
async def evaluate():
    evaluator = OnlineEvaluator()
    return evaluator.evaluate()


@api.on_event('startup')
@repeat_every(seconds=60*60*24)
def retrain():
    retrain_model()