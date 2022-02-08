import random

from fastapi import FastAPI
from inference import SVD_inference_fast

# Instantiating FastAPI
api = FastAPI()


# Defining a test root path and message
@api.get('/')
def root():
    return {'message': 'Welcome to Favor8!'}


# Defining the recommendation endpoint
@api.get('/recommend/{userid}')
async def recommend(userid: int):
    # ids = SVD_inference_fast(userid)
    top_100 = ['big+hero+6+2014', 'avatar+2009', 'gone+girl+2014', 'the+hunger+games+mockingjay+-+part+1+2014',
               'pulp+fiction+1994', 'the+dark+knight+2008', 'blade+runner+1982', 'the+avengers+2012',
               'the+maze+runner+2014', 'dawn+of+the+planet+of+the+apes+2014', 'whiplash+2014', 'fight+club+1999',
               'guardians+of+the+galaxy+2014', 'the+shawshank+redemption+1994', 'forrest+gump+1994',
               'pirates+of+the+caribbean+the+curse+of+the+black+pearl+2003', 'star+wars+1977', 'schindlers+list+1993',
               'rise+of+the+planet+of+the+apes+2011', 'the+godfather+1972', 'spirited+away+2001',
               'life+is+beautiful+1997', 'harry+potter+and+the+philosophers+stone+2001', 'psycho+1960', 'fury+2014',
               'the+godfather+part+ii+1974', 'lucy+2014', 'one+flew+over+the+cuckoos+nest+1975',
               'thor+the+dark+world+2013', 'dilwale+dulhania+le+jayenge+1995', 'the+twilight+saga+eclipse+2010',
               'pacific+rim+2013', 'the+matrix+1999', 'interstellar+2014', 'once+upon+a+time+in+america+1984',
               'the+lord+of+the+rings+the+fellowship+of+the+ring+2001', 'edge+of+tomorrow+2014',
               'the+hobbit+the+battle+of+the+five+armies+2014', 'the+imitation+game+2014',
               'pirates+of+the+caribbean+at+worlds+end+2007', 'twilight+2008', 'the+amazing+spider-man+2012',
               '12+years+a+slave+2013', 'chappie+2015', 'harry+potter+and+the+chamber+of+secrets+2002',
               'pirates+of+the+caribbean+dead+mans+chest+2006', 'the+lord+of+the+rings+the+two+towers+2002',
               'spider-man+2002', 'the+lord+of+the+rings+the+return+of+the+king+2003', 'thor+2011', 'inception+2010',
               'batman+begins+2005', 'harry+potter+and+the+prisoner+of+azkaban+2004',
               'kingsman+the+secret+service+2015', 'pirates+of+the+caribbean+on+stranger+tides+2011', 'insurgent+2015',
               'the+purge+anarchy+2014', 'spider-man+3+2007', 'dark+skies+2013', 'titanic+1997', 'pans+labyrinth+2006',
               'monsters_+inc.+2001', 'the+amazing+spider-man+2+2014', 'x-men+days+of+future+past+2014',
               'the+twilight+saga+breaking+dawn+-+part+1+2011', 'back+to+the+future+1985', 'finding+nemo+2003',
               'the+hunger+games+catching+fire+2013', 'kill+bill+vol.+1+2003', 'the+hangover+part+iii+2013',
               'harry+potter+and+the+deathly+hallows+part+2+2011', 'harry+potter+and+the+goblet+of+fire+2005',
               'despicable+me+2+2013', 'the+fifth+element+1997', 'frozen+2013', 'the+mummy+1999', 'iron+man+3+2013',
               'beauty+and+the+beast+1991', 'alien+1979', 'harry+potter+and+the+deathly+hallows+part+1+2010',
               'the+hobbit+an+unexpected+journey+2012', 'gladiator+2000', 'casino+royale+2006', 'the+jungle+book+1967',
               'terminator+2+judgment+day+1991', '2001+a+space+odyssey+1968', 'teenage+mutant+ninja+turtles+2014',
               'despicable+me+2010', 'the+incredibles+2004', 'penguins+of+madagascar+2014', 'ghostbusters+1984',
               'iron+man+2008', 'the+wizard+of+oz+1939', 'charlie+and+the+chocolate+factory+2005',
               'stonehearst+asylum+2014', 'toy+story+1995', 'aliens+1986', 'saving+private+ryan+1998',
               'the+lion+king+1994', 'divergent+2014']
    ids = random.sample(top_100, 20)
    return ','.join(x for x in ids)
