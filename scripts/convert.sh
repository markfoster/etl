# Convert pp*.xml* files to pp*.xml ready for import
ls pp_* | while read FILE; do echo mv "$FILE" "${FILE%%.*}.xml"; done

xmllint -o outfile.xml --format infile.xml
cat outfile | grep "<Action_Code>U" | wc -l >> audits

ls /var/tmp/etl/xml/pp_* | \
while read FILE; do 
   echo -n "$(basename $FILE),"; \
   echo -n $(cat $FILE | grep "<Action_Code>I" | wc -l); echo -n ","; \
   echo -n $(cat $FILE | grep "<Action_Code>U" | wc -l); echo -n ","; \
   echo $(cat $FILE | grep "<Action_Code>D" | wc -l); 
done > /var/tmp/report

xmllint -noout --schema xsd/PP_CHAPTER_XML.xsd --stream xml/pp_chapter_xml.xml 2>&1 >/dev/null || echo "Invalid"
