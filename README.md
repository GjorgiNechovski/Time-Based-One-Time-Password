# Time-Based-One-Time-Password

This is a simple banking simulation application that uses a generation and validation of a time based one time password.
It uses authentication and authorization made by me (It does not use Spring Security)

It has the following features:

- Generation and validation of a time based one time password
- Authentication and authorization (that doesn't include Spring Security)
- Uses Java's Email Sender
- MVC App
- Uses HTTPS (only for docker compose up and as long as the certificate hasn't expired)

To use docker compose all you need to do is run

- docker compose up
- The application will be running on localhost:8080 and localhost:443

To run the application using Kubernetes you need to run the following commands

- k3d cluster create totp -p "8081:80@loadbalancer"
- kubectl apply -f deployment.yaml
- The application will be running on localhost:8081
