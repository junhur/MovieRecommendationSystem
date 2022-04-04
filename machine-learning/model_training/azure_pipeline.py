from azureml.core.authentication import ServicePrincipalAuthentication
from azureml.core import Workspace, Experiment
from azureml.pipeline.core import PublishedPipeline
import json

with open("secrets.txt") as f:
    lines = f.readlines()
    TENANT_ID = lines[0].strip()
    CLIENT_ID = lines[1].strip()
    CLIENT_SECRET = lines[2].strip()
    SUBSCRIPTION_ID = lines[3].strip()

if __name__=='__main__':
    sp = ServicePrincipalAuthentication(tenant_id=TENANT_ID,  # tenantID
                                        service_principal_id=CLIENT_ID,  # clientId
                                        service_principal_password=CLIENT_SECRET)  # clientSecret

    ws = Workspace.get(name="recommender",
                       auth=sp,
                       subscription_id=SUBSCRIPTION_ID,
                       resource_group="favor8")

    ppl = PublishedPipeline.get(ws, id='0ab4967e-5fd2-42d4-a76a-7cfb9a8707d3')

    pipeline_run = Experiment(ws, 'recommender_train').submit(ppl)
    pipeline_run.wait_for_completion(show_output=True)

    metrics = pipeline_run.get_metrics()
    with open('metrics.json', 'a') as fp:
        json.dump(metrics, fp)