# Containers

Everything related to containers, kubernetes, Argo, ...

## Docker

###Build and Run

To create an image: `docker build -t <ImageName:Version> .`

To create and run a container: `docker run <Image> <CMD> <ARGS...>`

Common parameters:
- `--name <Name>`
- `--rm`: Delete container after run
- `-it`: Run interactively. Note: In this mode both sdterr and stdout are merged.
- `-d`: Detached mode

To run a sh command, CMD = sh, Args = <"-c", "echo bla">

To re-use the docker daemon within a container you need to bind mount
- `/var/run/docker.sock`
- `/usr/bin/docker`

###Management

- List images: `docker images`
- Remove an image: `docker rmi <ImageName or Id>`
- Remove a container: `docker rm <ContainerName or Id>`
- List volumes: `docker volume ls`
- List all containers: `docker ps [-a]` == `docker container ls [-a]` (Without `-a` shows only running containers)
- Show log: `docker logs <containerName or Id>`. `--follow` to follow the log of a running container
- Stop a container: `docker stop <ContainerName or Id>`
- Kill all running containers: `docker kill $(docker ps -q)`
- Delete all stopped containers: `docker rm $(docker ps -a -q)`
- Delete all images: `docker rmi $(docker images -q)`
- Save an image as tar: `docker save --output hello-world.tar <your image name or ID>`
- Load an image from tar: `docker load --input hello-world.tar`

##Kubernetes

Cf. https://kubernetes.io/docs/reference/kubectl/cheatsheet/


- View all contexts: `kubectl config view`
- Display current context: `kubectl config current-context`

In all the following commands you can apply to all namespaces with `--all-namespaces` or specify a namespace with `-n <namepace>`.
If no namespace is provided the default one is used (`default` if not modified in the context's config).

- Create an object from .yml or .json manifest: `kubectl create -f ./my-manifest.yaml`
- List services: `kubectl get services`
- List pods: `kubectl get pods [-o wide]` (`-o wide` provides more details)
- List running pods: `kubectl get pods --field-selector=status.phase=Running`
- List deployments: `kubectl get deployments`
- List secrets: `kubectl get secrets`. ATTENTION: `[-o json/yaml]` displays the secrets!!
- Get info about service/pod/deployment: `kubectl describe service/pod/deployment <x>`
- Display log of pod x: `kubectl logs <x>`

Equivalent of `docker run`: `kubectl ?run/create/apply? NAME --generator=run-pod/v1 --image=image [--env="key=value"] [--port=port] [--replicas=replicas] [--dry-run=bool] [--overrides=inline-json] [--command] -- [COMMAND] [args...] [options]`

## Minikube

To start minikube, run `minikube start`

Attention: Do NOT use `Ctrl+C` during start-up even if it takes some time. In case it happens, use `minikube delete && minikube start`

To delete minikube: `minikube delete`

Minikube is running in a VM which runs its own instance of docker daemon and does not have access to the host's docker daemon.
In order to get access to locally build docker images without using a docker registry, run `eval $(minikube docker-env)` in a terminal on the host.
Then any docker command will be executed on the VM's docker daemon. So any image built with `docker build` in this terminal will be accessible to minikube.

To see the kubernetes dashboard, run `minikube dashboard`, then copy-paste the URL in a browser if needed.

## Argo

Note: When running with minikube, cf tips above for using locally built images!

In order to get access to Argo GUI: `kubectl -n argo port-forward deployment/argo-ui 8001:8001`
The GUI is then accessible via `localhost:8001`

If following instructions to use Minio as artifact registry and installing on minikube cluster using Helm, the public IP is pending (probably due to https://stackoverflow.com/questions/44110876/kubernetes-service-external-ip-pending).
The solution is to do the same as above: `kubectl -n default port-forward deployment/argo-artifacts 9000:9000`, then access the GUI via `localhost:9000`.
Note: Following the url:port given by `sudo minikube service --url argo-artifacts` works as well.

- List all workflows: `argo list`
- Submit a workflow: `argo submit [--watch] <path.yaml>`
- Terminate a workflow: `argo terminate <workflowId>`
- Get info about workflow: `argo get <workflowId>`
- Get step log: `argo logs <podName>`

## Helm

In order to connect Helm to the Kubernetes cluster defined in your `~/.kube/config` (i.e. install and start Tiller on that cluster), run `helm init`.