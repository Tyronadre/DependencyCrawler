#!/bin/bash

# Path to the proto compiler
PROTOC_PATH="./protoc/bin/protoc.exe"

# Input folder containing .protoc files
INPUT_FOLDER="in/"

# Output folder for generated Java files
OUTPUT_FOLDER="out/"

# Create the output folder if it doesn't exist
mkdir -p "${OUTPUT_FOLDER}"

# Iterate over each .protoc file in the input folder
for file in in/*.proto; do
  filename=$(basename -- "$file")
	filename_no_ext="${filename%.*}"
  echo "Generating Java files for $filename_no_ext"
  ./protoc/bin/protoc.exe -I="in/" --java_out="out/" "${file}"
done
