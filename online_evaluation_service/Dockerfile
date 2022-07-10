FROM python:3.9

WORKDIR /app

COPY requirements.txt /app/requirements.txt
RUN pip3 install --no-cache --upgrade -r /app/requirements.txt

COPY . /app/
EXPOSE 7777

CMD uvicorn --host=0.0.0.0 --port=7777 app:app



