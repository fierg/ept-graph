#!/bin/bash

echo "Building from bash script..."
cd thesis || echo "Folder not found!"

latexmk -pdf -bibtex -f -silent thesis.tex

printf "\n\n\nDONE\n\n\n"
exit 0