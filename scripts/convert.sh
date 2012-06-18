# Convert pp*.xml* files to pp*.xml ready for import
ls pp_* | while read FILE; do echo mv "$FILE" "${FILE%%.*}.xml"; done
