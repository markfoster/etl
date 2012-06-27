#!/bin/bash
#
# ETL - Process any available XML files from the client
#
# Usage: CMS0294Notifications.sh [ -D ]
#
#    -D: Debug
#
# $Id: CMS0294Notifications.sh,v 1.11 2012/05/01 11:09:03 appadmin Exp $
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
    echo "usage: CMS0294Notifications.sh [ -D ]" 1>&2

    CleanUp
    exit 1
}

ErrorExit () {
    echo "CMS0294Notifications.sh:" "$*" 1>&2;

    CleanUp
    exit 1
}

# Cleanup on exit - we can't default to removing the lock if we don't own it

CleanUp ()
{
    exit 1
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
# Check Preview update status
#

CheckDrupalPreviewUpdated ()
{
    PrintInfo "CheckDrupalPreviewUpdated" 
    SQL="SELECT state FROM process_state WHERE entity='System'"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
    if [ "$QUERY" == "PREVIEW_DRUPAL_COMPLETE" ]; then
         PrintInfo "Query = ${QUERY}"
         return 1
    fi
    return 0
}

#
# Check the Watchdog table for alerts
#

CheckWatchdog ()
{
    SQL="SELECT link, severity, type, message, timestamp FROM watchdog WHERE severity < 3 AND timestamp > (NOW() - INTERVAL 10 MINUTE)"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
    if [ "$QUERY" != "" ]; then
         echo "Watchdog Alerts detected:"
         echo "${QUERY}"
         return 1
    else
         return 0
    fi
}

#
# Tidy up the Watchdog table
#

TidyWatchdog ()
{
    SQL="DELETE FROM watchdog WHERE timestamp < (NOW() - INTERVAL 30 DAY)"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
}

#
# Check Prod update status
#

CheckDrupalProdUpdated ()
{
    PrintInfo "CheckDrupalProdUpdated" 
    SQL="SELECT state FROM process_state WHERE entity='System'"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
    if [ "$QUERY" == "PROD_DRUPAL_COMPLETE" ]; then
         return 1
    fi
    return 0
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
    MAIL_FROM="CMS0294Notifications@$(hostname)"
    LOCK_ALERT=""

    echo "MailAlert ${1}:"

    (
	echo "To: ${EVENT_MAIL}"
	echo "From: ${MAIL_FROM}"

	case "${1}" in

	"LOCK_TIMEOUT" )	    echo "Subject: CRITICAL - CMS0294Notifications failed to get the Update lock on ${HostName}"
                                    LOCK_ALERT="LOCK"
                                    ;;
	"LOCK_CLEAR" )		    echo "Subject: CRITICAL - CMS0294Notifications failed to clear the Update lock on ${HostName}" 
                                    LOCK_ALERT="LOCK"
                                    ;;
	"DRUPAL_PREVIEW_UPDATED" )  echo "Subject: ALERT - Preview instance update has completed"
                                    echo "Priority: Urgent"
                                    echo "Importance: high"
                                    ;;
	"DRUPAL_PROD_UPDATED" )     echo "Subject: ALERT - Production instance update has completed" 
                                    echo "Priority: Urgent"
                                    echo "Importance: high"
                                    ;;
        "WATCHDOG_ALERT" )          echo "Subject: ALERT - Watchdog Alert detected"
                                    echo "Priority: Urgent"
                                    echo "Importance: high"
                                    DumpFiles "Context" ${LOG_FILE}
                                    ;;


	* )			    echo "Subject: WARNING - CMS0294Notifications unknown mail alert on ${HostName}" ;;

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

LockNotification ()
{
    mkdir -p "${LOCKS_DIR}" 2>/dev/null
    LOCK="prev_notification"
    case "${1}" in
        "DRUPAL_PREVIEW_UPDATED" ) 
              LOCK="prev_notification"
              ;;
        "DRUPAL_PROD_UPDATED" ) 
              LOCK="prod_notification"
              ;;
    esac
    printf "LockTime=[%s] Pid=%d Host=(%s)\n" "$(date '+%Y/%m/%d %H:%M:%S')" $$ $(hostname) > "${LOCKS_DIR}/${LOCK}"
}

CheckLockNotification ()
{
    LOCK="prev_notification"
    case "${1}" in
        "DRUPAL_PREVIEW_UPDATED" )
              LOCK="prev_notification"
              ;;
        "DRUPAL_PROD_UPDATED" )
              LOCK="prod_notification"
              ;;
    esac

    PrintInfo "CheckLock : ${LOCKS_DIR}/${LOCK}" 

    if [ -f "${LOCKS_DIR}/${LOCK}" ]; then
       PrintInfo "exists"
       return 0;
    fi
      
    return 1
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
else echo "CMS0294Notifications.sh: Environment file missing."
     exit 1
fi

# Flags

D_FLAG=""
v_FLAG=""
S_FLAG=""
F_FLAG=""

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294Notifications"
mkdir -p "${TMP_DIR}" 2>/dev/null

LOG_NAME="Notifications.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='Notifications.????-??-??_??:??:??.log.gz'

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

#echo "Starting Notifications at $(date)"

HostName="$(hostname | sed -e 's/\..*//')"

# Lock the process

     LOCKDIR="${XML_IN_DIR}_notify.lock"

     CheckWatchdog 
     if [ "$?" -eq "1" ]
     then MailAlert WATCHDOG_ALERT
          #ErrorExit "Watchdog alert detected"
     fi

     CheckDrupalPreviewUpdated
     if [ "$?" -eq "1" ]
     then CheckLockNotification DRUPAL_PREVIEW_UPDATED
          if [ "$?" -eq "1" ]
          then MailAlert DRUPAL_PREVIEW_UPDATED 
               #LockNotification DRUPAL_PREVIEW_UPDATED
               "/usr/local/bin/CMS0294ClearLocks.sh"
	       ErrorExit "Drupal Preview Database updated"
          fi
     fi

     CheckDrupalProdUpdated
     if [ "$?" -eq "1" ]
     then CheckLockNotification DRUPAL_PROD_UPDATED
          if [ "$?" -eq "1" ]
          then MailAlert DRUPAL_PROD_UPDATED 
               LockNotification DRUPAL_PROD_UPDATED
	       ErrorExit "Drupal Production Database updated"
          fi
     fi

     VerboseCheck 

# Completed

CleanUp
TidyWatchdog

#
# Cleanup old log files
#

for i in $(find "${TMP_DIR}" -type f -name "${LOG_MASK}" -mtime +30)
do
    PrintInfo "Removing old log file: $i"
    rm -f $i
done

#echo "Completed Notifications at $(date)"

exit 0;
