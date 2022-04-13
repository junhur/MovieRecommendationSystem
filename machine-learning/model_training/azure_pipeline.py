from azureml.core.authentication import ServicePrincipalAuthentication
from azureml.core import Workspace, Experiment
from azureml.pipeline.core import PublishedPipeline
import sys, json

if __name__=='__main__':
    try:
        with open("secrets.txt") as f:
            lines = f.readlines()
            TENANT_ID = lines[0].strip()
            CLIENT_ID = lines[1].strip()
            CLIENT_SECRET = lines[2].strip()
            SUBSCRIPTION_ID = lines[3].strip()
    except:
        print("Unable to read secrets.txt, read command line arguments instead")
        TENANT_ID = sys.argv[1]
        CLIENT_ID = sys.argv[2]
        CLIENT_SECRET = sys.argv[3]
        SUBSCRIPTION_ID = sys.argv[4]

    sp = ServicePrincipalAuthentication(tenant_id=TENANT_ID,  # tenantID
                                        service_principal_id=CLIENT_ID,  # clientId
                                        service_principal_password=CLIENT_SECRET)  # clientSecret

    ws = Workspace.get(name="new_recommender",
                       auth=sp,
                       subscription_id=SUBSCRIPTION_ID,
                       resource_group="favor8")

    ppl = PublishedPipeline.get(ws, id='0d26de90-95bc-4b5e-8253-d80556b9b290')

    pipeline_run = Experiment(ws, 'recommender-test').submit(ppl)
    pipeline_run.wait_for_completion(show_output=True)

    metrics = pipeline_run.get_metrics()
    print('Training metrics:\n{}'.format(metrics))

