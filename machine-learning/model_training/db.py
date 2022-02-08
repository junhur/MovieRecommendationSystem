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


def get_movie_info():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT title, info->>'original_title',
                          (info->>'adult')::BOOLEAN, (info->>'budget')::NUMERIC, (info->>'revenue')::NUMERIC, info->>'runtime', 
                          (info->>'popularity')::NUMERIC, (info->>'vote_count')::NUMERIC, (info->>'vote_average')::NUMERIC,
                          info->>'release_date', info->>'original_language', info->>'overview',
                          info->>'genres', info->>'production_companies', info->>'production_countries'
    			   FROM movies''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results,
                        columns=['movie_title', 'original_title', 'adult', 'budget', 'revenue', 'runtime', 'popularity',
                                 'vote_count',
                                 'vote_average',
                                 'release_date', 'original_language', 'overview', 'genres_json', 'companies_json',
                                 'countries_json'])


def get_user_ratings():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT user_id, movie_title, score
				   FROM rating''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results, columns=['user_id', 'movie_title', 'score'])


def get_user_info():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT id, age, occupation, gender
    				   FROM users''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results, columns=['user_id', 'user_age', 'user_occupation', 'user_gender'])


def get_user_watching_history():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT user_id, movie_title, MIN(minute), MAX(minute), (info->>'runtime')::NUMERIC 
    				   FROM watching LEFT JOIN movies ON watching.movie_title=movies.title
    				   GROUP BY user_id, movie_title, info->>'runtime' ''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results, columns=['user_id', 'movie_title', 'start_time', 'end_time', 'runtime'])


if __name__ == '__main__':
    print(get_most_popular_movie_ids(100))
