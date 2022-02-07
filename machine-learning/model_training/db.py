import psycopg2
import pandas as pd

## DB information
DB_HOST = 'localhost'
DB_PORT = 5432
DB_NAME = 'movielog'
DB_USER = 'xiangyuy'
DB_PSWD = 'password'


def get_most_popular_movie_ids(num_ids):
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT title
					FROM movies
				   ORDER BY (info->>'popularity')::NUMERIC DESC
				   FETCH FIRST {} ROWS ONLY'''.format(num_ids))
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return [q[0] for q in query_results]


def get_user_ratings():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT user_id, movie_title, score
					FROM rating''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results, columns=['user_id', 'movie_title', 'score'])
