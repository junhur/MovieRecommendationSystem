from fastapi import FastAPI
from inference import CF_inference_fast

inference_algo_name = 'SVD'

# Instantiating FastAPI
api = FastAPI()


# Defining a test root path and message
@api.get('/')
def root():
    return {'message': 'Welcome to Favor8!'}


# Defining the recommendation endpoint
@api.get('/recommend/{userid}')
async def recommend(userid: int):
    ids = CF_inference_fast(inference_algo_name, userid)
    return ','.join(x for x in ids)
