# Containers

Everything related to containers, kubernetes, Argo, ...

## Docker

###Build and Run

To create an image: `docker build -t [ImageName:Version] .`

To create and run a container: `docker run [Image] [CMD] [ARGS...]`

Common parameters:
- `--name [Name]`
- `--rm`: Delete container after run
- `-it`: Run interactively. Note: In this mode both sdterr and stdout are merged.
- `-d`: Detached mode

To run a sh command, CMD = sh, Args = ["-c", "echo bla"]

###Management

- List images: `docker images`
- Remove an image: `docker rmi [ImageName or Id]`
- Remove a container: `docker rm [ContainerName or Id]`
- List volumes: `docker volume ls`
- List all containers: `docker ps -a` == `docker container ls -a` (Without `-a` shows only running containers)
- Show log: `docker logs [containerName or Id]`. `--follow` to follow the log of a running container
- Stop a container: `docker stop [ContainerName or Id]`
- Kill all running containers: `docker kill $(docker ps -q)`
- Delete all stopped containers: `docker rm $(docker ps -a -q)`
- Delete all images: `docker rmi $(docker images -q)`
- Save an image as tar: `docker save --output hello-world.tar [your image name or ID]`
- Load an image from tar: `docker load --input hello-world.tar`


## Minikube

To start minikube, run `sudo minikube start`

Attention: Do NOT use `Ctrl+C` during start-up even if it takes some time. In case it happens, use `sudo minikube delete && sudo minikube start`

To stop minikube: `sudo minikube stop`

Minikube is running in a VM which runs its own instance of docker daemon and does not have access to the host's docker daemon.
In order to get access to locally build docker images without using a docker registry, run `eval $(sudo minikube docker-env)` in a terminal on the host.
Then any docker command will be executed on the VM's docker daemon. So any image built with `docker build` in this terminal will be accessible to minikube.

To see the kubernetes dashboard, run `sudo minikube dashboard`, then copy-paste the URL in a browser if needed.

## Argo

In order to get access to Argo GUI: `kubectl -n argo port-forward deployment/argo-ui 8001:8001`
The GUI is then accessible via `localhost:8001`

- List all workflows: `argo list`
- Submit a workflow: `argo submit --watch [path.yaml]` (--watch optional)
- Terminate a workflow: `argo terminate [workflowId]`
- Get info about workflow: `argo get [workflowId]`
- Get step log: `argo logs [podName]`