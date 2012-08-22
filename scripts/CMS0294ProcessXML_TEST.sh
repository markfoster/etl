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
        echo "Priority: Urgent"
        echo "Importance: high"

	case "${1}" in

	"XML_TEST_AVAILABLE" )  echo "To: ${T_ALERT_MAIL}"
                                echo "Subject: TEST - CQC XML available" ;;

	* )			echo "To: ${ALERT_MAIL}"
                                echo "Subject: WARNING - CMS0294ProcessXML unknown mail alert on ${HostName}" ;;

	esac

	echo

        #DumpFiles "Switch log content" "${LOG_FILE}"
	if   [ "${1}" == "XML_TEST_AVAILABLE" ]
	then 
             DumpFiles "Log content" "${LOG_FILE}"
	fi

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

TMP_DIR="/var/tmp/CMS0294ProcessXMLTEST"
mkdir -p "${TMP_DIR}" 2>/dev/null

LOG_NAME="PreviewLoadTEST.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='PreviewLoadTEST.????-??-??_??:??:??.log.gz'

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

export XML_IN_TEST_DIR="/mnt/www/etltest"

# Lock the process

     # The pp_audit_xml.* file should be the last one written in an upload
     # We wait until this file is at least 5 minutes old before processing
     fProcess=0
     PP_AUDIT="pp_z_audit_xml*"
     MMIN="+5"

     # find an audit file older than 5 minutes
     if [ $(find ${XML_IN_TEST_DIR} -maxdepth 1 -type f -mmin ${MMIN} -iname "${PP_AUDIT}" | wc -l) -gt 0 ]; then

         # find the number of files in the directory
         FILE_COUNT=$(ls -ltr $XML_IN_TEST_DIR/pp_*.xml* | wc -l)

         # check for the last updated file in the directory
         LAST_CHANGED_FILE=$(ls -t1 $XML_IN_TEST_DIR | head -n1)
         LAST_CHANGED=$(stat -c %Y $XML_IN_TEST_DIR/$LAST_CHANGED_FILE)
         LAST_NOW=$(date +%s)
         let LAST_ELAPSED=LAST_NOW-LAST_CHANGED

         echo "Last file changed $LAST_ELAPSED seconds ago"

         # ...we need at least 16 files and it needs to be older than 5 minutes 
         if [ $FILE_COUNT -gt 16 -a $LAST_ELAPSED -gt 300 ]; then
            fProcess=1
         fi
     
     fi

     # ...assuming that the above conditions have been met then continue the processing...
     if [ $fProcess -gt 0 ]; then

        echo "Listing of '$XML_IN_TEST_DIR':"
        ls -ltr "$XML_IN_TEST_DIR"

        MailAlert XML_TEST_AVAILABLE

        bDate=$(date '+%Y%m%d_%H%M%S')
        tar zcvf ${BACKUP_DIR}/xml_test_in_${bDate}.tgz ${XML_IN_TEST_DIR}/pp* > /dev/null 2>&1
        rm -f ${XML_IN_TEST_DIR}/*

     fi

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

echo "Completed Preview Update at $(date)"

exit 0;
