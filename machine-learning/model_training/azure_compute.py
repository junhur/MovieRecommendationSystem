from azureml.core import Workspace
from azureml.core.compute import ComputeInstance
from azureml.core.compute_target import ComputeTargetException
from azureml.core.authentication import ServicePrincipalAuthentication

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

    # Choose a name for your instance
    # Compute instance name should be unique across the azure region
    compute_name = "recommender-train"

    # Verify that instance does not exist already
    try:
        instance = ComputeInstance(workspace=ws, name=compute_name)
        print('Found existing instance, use it.')
    except ComputeTargetException:
        compute_config = ComputeInstance.provisioning_configuration(
            vm_size='STANDARD_F2S_V2',
            ssh_public_access=False
        )
        instance = ComputeInstance.create(ws, compute_name, compute_config)
        instance.wait_for_completion(show_output=True)