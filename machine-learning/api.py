from fastapi import FastAPI

from data_access.db import get_latest_model_version
from model_inference.inference import CF_inference_fast
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
