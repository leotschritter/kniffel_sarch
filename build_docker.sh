#!/bin/bash

# List of module directories containing Dockerfiles
MODULES=("dicecup" "field" "fileio" "game" "tui" "restcontroller" "gui")

# Function to build Dockerfile in a directory
build_dockerfile() {
    local module=$1
    echo "Building Dockerfile in $module..."
    docker build -t "$module"-image:latest $module
}

# Loop through each module and build Dockerfile
for module in "${MODULES[@]}"; do
    build_dockerfile "$module"
done

echo "All Dockerfiles built successfully!"
