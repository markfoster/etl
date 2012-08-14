#!/bin/bash
#
# ETL - Process any available XML files from the client
#
# Usage: CMS0294ProductionUpdate.sh [ -D ]
#
#    -D: Debug
#
# $Id: CMS0294ProductionUpdate.sh,v 1.11 2012/05/01 11:09:03 appadmin Exp $
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
    echo "usage: CMS0294ProductionUpdate.sh [ -D ]" 1>&2

    CleanUp
    exit 1
}

ErrorExit () {
    echo "CMS0294ProductionUpdate.sh:" "$*" 1>&2;

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
# Check we are in IDLE state
#

CheckTriggerState ()
{
    SQL="SELECT state FROM process_state WHERE entity='System'"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
    if [ "$QUERY" == "PROD_LOAD_TRIGGER" ]; then
         return 1
    fi
    return 0
}

BackupProduction()
{
    PrintInfo "BackupProduction"
    bDate=$(date '+%Y%m%d_%H%M%S')
    QUERY=`mysqldump --skip-extended-insert -h ${PROD_HOST} -u ${PROD_USER} --password=${PROD_CRED} ${PROD_DB} > ${BACKUP_DIR}/PROD_DB_${bDate}.sql`
    return 0
}

#
# Send Mail Alert
#
# 1: SOLR or WEB message
#

MailAlert ()
{
    MAIL_FROM="CMS0294ProductionUpdate@$(hostname)"
    LOCK_ALERT=""

    (
	case "${1}" in

	"LOCK_TIMEOUT" )	    echo "To: ${ALERT_MAIL}"
                                    echo "From: ${MAIL_FROM}"
                                    echo "Subject: CRITICAL - CMS0294ProductionUpdate failed to get the Update lock on ${HostName}"
                                    LOCK_ALERT="LOCK"
                                    ;;
	"LOCK_CLEAR" )		    echo "To: ${ALERT_MAIL}" 
                                    echo "From: ${MAIL_FROM}"
                                    echo "Subject: CRITICAL - CMS0294ProductionUpdate failed to clear the Update lock on ${HostName}" 
                                    LOCK_ALERT="LOCK"
                                    ;;
	"DRUPAL_PREVIEW_UPDATED" )  echo "To: ${EVENT_MAIL}" 
                                    echo "From: ${MAIL_FROM}"
                                    echo "Subject: ALERT - Preview instance update has completed"
                                    echo "Priority: Urgent"
                                    echo "Importance: high"
                                    ;;
	"DRUPAL_PROD_UPDATED" )     echo "To: ${EVENT_MAIL}"
                                    echo "From: ${MAIL_FROM}"
                                    echo "Subject: ALERT - Production instance update has completed" 
                                    echo "Priority: Urgent"
                                    echo "Importance: high"
                                    ;;

	* )			    echo "To: ${ALERT_MAIL}" 
                                    echo "From: ${MAIL_FROM}"
                                    echo "Subject: WARNING - CMS0294ProductionUpdate unknown mail alert on ${HostName}" ;;

	esac

	echo

        if   [ "${LOCK_ALERT}" == "LOCK" ]; then
	  if   [ "${LOCKDIR}" != "" ]
	  then DumpFiles "Lock file (${LOCKDIR}/owner}) content" "${LOCKDIR}/owner"
	  fi
	fi
	
	echo
    ) | /usr/lib/sendmail -f "${MAIL_FROM}" -t
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
else echo "CMS0294ProductionUpdate.sh: Environment file missing."
     exit 1
fi

# Flags

D_FLAG=""
v_FLAG=""
S_FLAG=""
F_FLAG=""

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294ProductionUpdate"
mkdir -p "${TMP_DIR}" 2>/dev/null

LOG_NAME="ProductionUpdate.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='ProductionUpdate.????-??-??_??:??:??.log.gz'

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

echo "Starting Production Update at $(date)"

HostName="$(hostname | sed -e 's/\..*//')"

# Lock the process and restart jetty

     LOCKDIR="${XML_IN_DIR}_prod.lock"

     if   LockProcess
     then MailAlert LOCK_TIMEOUT
	  ErrorExit "Did not get the Update lock"
     fi

     JAVA_CLASSPATH="/opt/etl/build:/opt/etl/lib/*:/opt/etl/conf:/opt/etl/hbm"
     JAVA_STUB="ETLProductionLoad"

     CheckTriggerState
     if [ "$?" -eq "1" ]
     then BackupProduction
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

echo "Completed Production Update at $(date)"

exit 0;
