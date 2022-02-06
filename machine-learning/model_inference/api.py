import dill as pickle
from fastapi import FastAPI, Request
from pydantic import BaseModel


## API INSTANTIATION
## -----------------

# Instantiating FastAPI
api = FastAPI()

# Loading in model from serialized .pkl file
# model_pkl = '../model_trained/dummy.pkl'
# model_pkl = '../model_trained/latest.pkl' # TODO: version control

# with open(model_pkl, 'rb') as file:
	# model = pickle.load(file)


## API ENDPOINTS
## -------------

# Defining a test root path and message
@api.get('/')
def root():
	return {'message': 'Welcom to Favor8!'}

# Defining the recommendatin endpoint
@api.get('/recommend/{userid}')
async def recommend(userid: int):

	dummy_ids = ['big+hero+6+2014', 'avatar+2009', 'gone+girl+2014', 'the+hunger+games+mockingjay+-+part+1+2014', 'pulp+fiction+1994', 'the+dark+knight+2008', 'blade+runner+1982', 'the+avengers+2012', 'the+maze+runner+2014', 'dawn+of+the+planet+of+the+apes+2014', 'whiplash+2014', 'fight+club+1999', 'guardians+of+the+galaxy+2014', 'the+shawshank+redemption+1994', 'forrest+gump+1994', 'pirates+of+the+caribbean+the+curse+of+the+black+pearl+2003', 'star+wars+1977', 'schindlers+list+1993', 'rise+of+the+planet+of+the+apes+2011', 'the+godfather+1972']
	print(','.join(x for x in dummy_ids))
	return ','.join(x for x in dummy_ids)
	# return model(userid)