#!/bin/bash
# Run script for Python version of Facebook Rental Crawler

# Load environment variables from .env file if it exists
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Check if scroll count is provided
if [ -z "$1" ]; then
    echo "Usage: ./run_python.sh <SCROLL_COUNT>"
    echo "Example: ./run_python.sh 10"
    exit 1
fi

# Add Python source directory to PYTHONPATH
export PYTHONPATH="${PYTHONPATH}:$(pwd)/src/python"

# Run the crawler
python -m xyz.jessyu.main "$1"
