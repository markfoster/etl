#!/bin/bash
#
# ETL - Process any available XML files from the client
#
# Usage: CMS0294ProcessXML.sh [ -D ]
#
#    -D: Debug
#
# $Id: CMS0294ProcessXML.sh,v 1.11 2012/05/01 11:09:03 appadmin Exp $
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   01/06/12	01.00	MF	Initial version
#   02/06/12	01.01	MF	Add process lock and fix link
#				to create the directory link not
#				link in directory.
#   01/05/12	01.09	IS	Add some debug information
#   01/05/12	01.10	IS	Add process lock on master debug information
#

#
# Usage functions
#

Usage () {
    echo "usage: CMS0294ProcessXML.sh [ -D ]" 1>&2

    CleanUp
    exit 1
}

ErrorExit () {
    echo "CMS0294ProcessXML.sh:" "$*" 1>&2;

    CleanUp
    exit 1
}

# Cleanup on exit - we can't default to removing the lock if we don't own it

CleanUp ()
{
    if   [ "${LOCKDIR}" != "" ] && fgrep "Host=($(hostname))" "${LOCKDIR}/owner" 
    then rm -rf "${LOCKDIR}"
    fi
}

#
# Locking process
#
# Create lock and set up traps
#

LockProcess ()
{
    if   mkdir -m 0755 "${LOCKDIR}"
    then printf "LockTime=[%s] Pid=%d Host=(%s)\n" "$(date '+%Y/%m/%d %H:%M:%S')" $$ $(hostname) > "${LOCKDIR}/owner"

         trap "{ UnLockProcess; exit 255; }" 1 2 3 9 15
	 return 1

    else return 0
    fi
}

#
# Delete lock and clear traps
#

UnLockProcess ()
{
    if   [ "${LOCKDIR}" = "" ]
    then return

    elif rm -r "${LOCKDIR}"
    then LOCKDIR=""
         trap "" 1 2 3 9 15

    else MailAlert LOCK_CLEAR
	 ErrorExit "Lock removal failed"
    fi
}

#
# Sleep for a random time up to ${1} seconds and for a minimum of ${2} seconds
#
# 1: Sleep time
# 2: Min sleep time

RanSleep () {
    if   [ "${2}" = "" ]
    then sleep $(((${RANDOM} * ${1}) / 32767))
    else sleep $(( ${2} + ((${RANDOM} * (${1} - ${2})) / 32767) ))
    fi
}

#
# Verbose Check on index in use
#

VerboseCheck () {
    if   [ "${v_FLAG}" = "YES" ]
    then PrintInfo "Verbose Check"
    fi
}

#
# Send Mail Alert
#
# 1: SOLR or WEB message
#

MailAlert ()
{
    MAIL_FROM="CMS0294ProcessXML@$(hostname)"

    (
	echo "From: ${MAIL_FROM}"

	case "${1}" in

	"LOCK_TIMEOUT" )	echo "To: ${ALERT_MAIL}"
                                echo "Subject: CRITICAL - CMS0294ProcessXML failed to get the Update lock on ${HostName}" ;;
	"LOCK_CLEAR" )		echo "To: ${ALERT_MAIL}"
                                echo "Subject: CRITICAL - CMS0294ProcessXML failed to clear the Update lock on ${HostName}" ;;
	"XML_AVAILABLE" )       echo "To: ${EVENT_MAIL}"
                                echo "Subject: INFORMATION - XML available, processing..." ;;
	"BAD_XML_FORMAT" )      echo "To: ${EVENT_MAIL}"
                                echo "Subject: CRITICAL - XML formating issues" ;;
	"INVALID_STATE" )       echo "To: ${ALERT_MAIL}"
                                echo "Subject: ALERT - XML detected but not in IDLE state on ${HostName}" ;;
	"SIZE_EXCEEDED_IN_OFFICE_HOURS" )  
                                echo "To: ${EVENT_MAIL}"
                                echo "Subject: ALERT - XML file size exceeds maximum sizes" ;;

	* )			echo "To: ${ALERT_MAIL}"
                                echo "Subject: WARNING - CMS0294ProcessXML unknown mail alert on ${HostName}" ;;

	esac

	echo

        #DumpFiles "Switch log content" "${LOG_FILE}"
	if   [ "${1}" == "XML_AVAILABLE" ]
	then DumpAudit 
	fi

	if   [ "${1}" == "BAD_XML_FORMAT" ]
	then echo "Priority: Urgent"
             echo "Importance: high"
             DumpFiles "Log content" "${LOG_FILE}"
	fi

        if   [ "${1}" == "SIZE_EXCEEDED_IN_OFFICE_HOURS" ]
        then echo "Priority: Urgent"
             echo "Importance: high"
             DumpFiles "Log content" "${LOG_FILE}"
        fi

	#if   [ "${LOCKDIR}" != "" ]
	#then DumpFiles "Lock file (${LOCKDIR}/owner}) content" "${LOCKDIR}/owner"
	#fi
	
	echo
    ) | /usr/lib/sendmail -f "${MAIL_FROM}" -t
}

#
# Check we are in IDLE state
#

CheckIdleState ()
{
    SQL="SELECT state FROM process_state WHERE entity='System'"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
    if [ "$QUERY" == "IDLE" ]; then
         return 1
    fi
    return 0
}

#
# Check a parameter is numeric
#

CheckNumericValue ()
{
    b=$(echo $1 | sed -e 's/[^0-9]//g')

    if [ "${1}" != "${b}" ]
    then ErrorExit "Bad Number for $2 (${1})"
    fi
}

#
# Find our commands
#

CMD_PATH=$(dirname "${0}")

if  [ "${CMD_PATH}" = "." ]
then CMD_PATH="${PWD}"
elif [ "${CMD_PATH}" = ".." ]
then CMD_PATH="${PWD}/.."
fi


# Load the environment parameters

if   [ -f "${CMD_PATH}/CMS0294Environment.sh" ]
then . "${CMD_PATH}/CMS0294Environment.sh"
elif [ -f "/usr/local/bin/CMS0294Environment.sh" ]
then . "/usr/local/bin/CMS0294Environment.sh"
else echo "CMS0294ProcessXML.sh: Environment file missing."
     exit 1
fi

# Flags

D_FLAG=""
v_FLAG=""
S_FLAG=""
F_FLAG=""

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294ProcessXML"
mkdir -p "${TMP_DIR}" 2>/dev/null

LOG_NAME="PreviewLoad.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='PreviewLoad.????-??-??_??:??:??.log.gz'

# Check Options

while getopts "FDv" op
do
    case "${op}" in

    "D" ) D_FLAG="YES" ;;
    "F" ) F_FLAG="YES" ;;
    "v" ) v_FLAG="YES" ;;

    * ) Usage ;;

    esac
done

shift $((OPTIND - 1))

#if   [ $# -ne 0 -o "${S_FLAG}" = "" ]
#then Usage
#fi

# Save the old log files

if   [ -f "${LOG_FILE}" ]
then d=$(stat -c '%y' "${LOG_FILE}" | cut -c 1-19 | tr ' ' '_')
     gzip -9f "${LOG_FILE}"
     mv -f "${LOG_FILE}.gz" "${LOG_FILE}.${d}.gz"
fi

# Start off

if   [ "${D_FLAG}" = "" ]
then exec >"${LOG_FILE}" 2>&1
fi

echo "Starting Preview Update at $(date)"

HostName="$(hostname | sed -e 's/\..*//')"

# Lock the process
     LOCKDIR="${XML_IN_DIR}_xml.lock"

     if   LockProcess
     then MailAlert LOCK_TIMEOUT
	  ErrorExit "Did not get the XML Update lock"
     fi

     # The pp_z_audit_xml.* file should be the last one written in an upload
     # We wait until this file is at least 5 minutes old before processing
     fProcess=0
     PP_AUDIT="pp_z_audit_xml*"
     MMIN="+5"

     # find an audit file older than 5 minutes
     if [ $(find ${XML_IN_DIR} -maxdepth 1 -type f -mmin ${MMIN} -iname "${PP_AUDIT}" | wc -l) -gt 0 ]; then

         # find the number of files in the directory
         FILE_COUNT=$(ls -ltr $XML_IN_DIR/pp_*.xml* | wc -l)

         # check for the last updated file in the directory
         LAST_CHANGED_FILE=$(ls -t1 $XML_IN_DIR | head -n1)
         LAST_CHANGED=$(stat -c %Y $XML_IN_DIR/$LAST_CHANGED_FILE)
         LAST_NOW=$(date +%s)
         let LAST_ELAPSED=LAST_NOW-LAST_CHANGED

         # ...we need at least 16 files and it needs to be older than 5 minutes 
         if [ $FILE_COUNT -gt 16 -a $LAST_ELAPSED -gt 300 ]; then
            fProcess=1
         fi
     
     fi

     # ...assuming that the above conditions have been met then continue the processing...
     if [ $fProcess -gt 0 ]; then

        CheckIdleState
        if [ "$?" -eq "0" ]
        then MailAlert INVALID_STATE
             ErrorExit "XML detected, but not at IDLE"
        fi

        bDate=$(date '+%Y%m%d_%H%M%S')
        tar zcvf ${BACKUP_DIR}/xml_in_${bDate}.tgz ${XML_IN_DIR}/pp* > /dev/null 2>&1

        rm -rf "${XML_PROCESS_DIR}"
        mkdir -p "${XML_PROCESS_DIR}" 2>/dev/null

        # MSF: 24/07/12 - New pp_z_audit_xml.* file as audit file
        ls ${XML_IN_DIR}/pp_z_aud* | while read FILE;
        do
            mv "$FILE" "${XML_PROCESS_DIR}/pp_audit_xml.xml"
        done
        mv ${XML_IN_DIR}/pp_* "${XML_PROCESS_DIR}" > /dev/null 2>&1

        # Trigger the mail alert
        MailAlert XML_AVAILABLE

        # Check the maximum file size and whether we are running in office hours
	MAX_TMP=`du -k ${XML_PROCESS_DIR}/* | sort -r -n | head -n 1`
	MAX_SIZE=`echo "${MAX_TMP}" | cut -f 1`
	MAX_FILE=`echo "${MAX_TMP}" | cut -f 2`
        MAX_FILE=$(basename ${MAX_FILE})
	if [[ $MAX_SIZE -gt $SizeLimit ]]; then
	   HOUR=$(date +%k)
	   if [[ $HOUR -ge $OfficeHoursStart ]] && [[ $HOUR -le $OfficeHoursEnd ]]; then
	     echo "Attempt to run large XML ETL during office hours."
	     echo "XML file '${MAX_FILE}' size, ${MAX_SIZE}k, exceeds max size of ${SizeLimit}k"
	     dS=$(date --date="${OfficeHoursStart}:00" +%H:%M%p)
	     dE=$(date --date="${OfficeHoursEnd}:00" +%H:%M%p)
	     echo "Office hours are currently set to Monday to Friday ${dS} to ${dE}"
             MailAlert SIZE_EXCEEDED_IN_OFFICE_HOURS
             ErrorExit "Max file size exceeded, in office hours"
	   fi
	fi

        # Convert pp*.xml* files to pp*.xml ready for import
        ls ${XML_PROCESS_DIR}/pp_* | while read FILE; do mv "$FILE" "${FILE%%.*}.xml"; done

        # format the XML (optional step)
        formatIssue=0
        rm -f ${XML_PROCESS_DIR}/report.csv
        for FILE in $(find ${XML_PROCESS_DIR} -type f -iname "pp_*.xml")
        do
           FBASE=$(basename ${FILE%%.*})
           echo "Validating and formating XML ${FBASE}:"
           xmllint --format "$FILE" -o "$FILE"
           result=$?
           if [ $result -gt 0 ]; then
              formatIssue=1
           else
              EBASE=$(echo ${FBASE} | sed -e 's/pp_//g' | sed -e 's/_xml//g')
              iI=$(cat $FILE | grep "<Action_Code>I" | wc -l)
              iU=$(cat $FILE | grep "<Action_Code>U" | wc -l)
              iD=$(cat $FILE | grep "<Action_Code>D" | wc -l)
              echo "${EBASE},${iI},${iU},${iD}" >> ${XML_PROCESS_DIR}/report.csv
           fi
        done
        if [ $formatIssue -gt 0 ]; then
           MailAlert BAD_XML_FORMAT
           ErrorExit "XML Formatting issues" 
        fi

        # validate the XML against the XSD
        formatIssue=0
        for FILE in $(find ${XML_PROCESS_DIR} -type f -iname "pp_*.xml")
        do
           FBASE=$(basename ${FILE%%.*})
           XSD=$(echo $FBASE | tr '[a-z]' '[A-Z]')
           XSD="/opt/etl/xsd/$XSD.xsd"
           echo "Validating XML ${FBASE} against XSD:"
           xmllint -noout --schema "$XSD" --stream "$FILE"
           result=$?
           if [ $result -gt 0 ]; then
              formatIssue=1
           fi
        done
        if [ $formatIssue -gt 0 ]; then
           MailAlert BAD_XML_FORMAT
           ErrorExit "XML Formatting issues"
        fi

        # ...if available then run the upload task
        JAVA_CLASSPATH="/opt/etl/build:/opt/etl/lib/*:/opt/etl/conf:/opt/etl/hbm"
        JAVA_STUB="ETLPreviewLoad"
        echo "java ${JAVA_OPTS} -classpath ${JAVA_CLASSPATH} ${JAVA_STUB}"
        java ${JAVA_OPTS} -classpath ${JAVA_CLASSPATH} ${JAVA_STUB}
     fi

# Remove the lock
     UnLockProcess
     LOCKDIR=""

     VerboseCheck 

# Completed
CleanUp

#
# Cleanup old log files
#

for i in $(find "${TMP_DIR}" -type f -name "${LOG_MASK}" -mtime +30)
do
    PrintInfo "Removing old log file: $i"
    rm -f $i
done

# remove small log files
for i in $(find /var/tmp -type f -size -800c -iname "*.gz")
do
    PrintInfo "Removing old log file: $i"
    rm -f $i
done

# remove small log files
for i in $(find /var/tmp -type f -mtime +3 -iname "*.sql")
do
    PrintInfo "Removing old backup SQL file: $i"
    rm -f $i
done

echo "Completed Preview Update at $(date)"

exit 0;
