# Convert pp*.xml* files to pp*.xml ready for import
ls pp_* | while read FILE; do echo mv "$FILE" "${FILE%%.*}.xml"; done

xmllint -o outfile.xml --format infile.xml
cat outfile | grep "<Action_Code>U" | wc -l >> audits
