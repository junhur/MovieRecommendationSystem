from azureml.core.authentication import ServicePrincipalAuthentication
from azureml.core import Workspace, Experiment
from azureml.pipeline.core import PublishedPipeline

if __name__=='__main__':
    sp = ServicePrincipalAuthentication(tenant_id="f70874a7-6e94-4cc9-9a3a-61dbe23c9955",  # tenantID
                                        service_principal_id="fa700399-a952-4f65-85fe-d2b3ff88007e",  # clientId
                                        service_principal_password="kZipvNWoqj7TLVocijFDFdS~URZ1gOEU70")  # clientSecret

    ws = Workspace.get(name="recommender",
                       auth=sp,
                       subscription_id="f4126c71-f302-4cde-9386-b42a907f292d",
                       resource_group="favor8")

    ppl = PublishedPipeline.get(ws, id='b92d76ae-f183-4e6f-89cb-1fa5ea778e9b')

    pipeline_run = Experiment(ws, 'recommender_train').submit(ppl)
    pipeline_run.wait_for_completion(show_output=True)