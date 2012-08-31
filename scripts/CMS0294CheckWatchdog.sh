#!/bin/bash
#
# ETL - Clear any active locks
#
# Usage: CMS0294CheckWatchdog.sh [ -D ]
#
#    -D: Debug
#
# $Id: CMS0294CheckWatchdog.sh,v 1.11 2012/05/01 11:09:03 appadmin Exp $
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   01/06/12	01.00	MF	Initial version
#

#
# Usage functions
#

Usage () {
    echo "usage: CMS0294CheckWatchdog.sh [ -D -n count -u uid ]" 1>&2

    CleanUp
    exit 1
}

ErrorExit () {
    echo "CMS0294CheckWatchdog.sh:" "$*" 1>&2;

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

    fi
}


# Delete lock and clear traps
#

UnLockDatabaseLocks ()
{
    SQL="UPDATE process_state SET state = 'CLEAR' WHERE entity='Lock'"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
    SQL="UPDATE process_state SET state = 'IDLE' WHERE entity='System'"
    QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
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
    then # Unlock the database locks and reset the system state
         SQL="select timestamp,wid,uid,message from common.watchdog where message not like '%Memory%' and message not like 'Sched%' and message not like 'Skipp%' "
         if [ "${U_FLAG}" = "YES" ]
         then SQL="${SQL} AND uid = $uid"
         fi
         SQL="${SQL} order by wid desc"
         if [ "${N_FLAG}" = "YES" ]
         then SQL="${SQL} limit $ncount"
         else SQL="${SQL} limit 20"
         fi
         echo "mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}"
         QUERY=`mysql -e "${SQL}" --skip-column-names --raw -h ${COMMON_HOST} -u ${COMMON_USER} --password=${COMMON_CRED} ${COMMON_DB}`
         echo "${QUERY}"
    fi
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
else echo "CMS0294CheckWatchdog.sh: Environment file missing."
     exit 1
fi

# Flags

D_FLAG="YES"
v_FLAG="YES"
S_FLAG=""
F_FLAG=""
N_FLAG=""
U_FLAG=""

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294CheckWatchdog"
mkdir -p "${TMP_DIR}" 2>/dev/null

LOG_NAME="CheckLocks.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='CheckLocks.????-??-??_??:??:??.log.gz'

# Check Options

while getopts ":n:u:Dv" op
do
    case "${op}" in

    "D" ) D_FLAG="YES" ;;
    "v" ) v_FLAG="YES" ;;
    "n" ) N_FLAG="YES" 
          ncount=$OPTARG 
          ;;
    "u" ) U_FLAG="YES" 
          uid=$OPTARG 
          ;;

    * ) Usage ;;

    esac
done

shift $((OPTIND - 1))

#if   [ $# -ne 0 -o "${S_FLAG}" = "" ]
#then Usage
#fi

if    [ "${U_FLAG}" = "YES" ]
then echo "UID = $uid"
fi

if    [ "${N_FLAG}" = "YES" ]
then echo "Count= $ncount"
fi

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

HostName="$(hostname | sed -e 's/\..*//')"

# Remove the lock

     #UnLockProcess
     LOCKDIR=""
     VerboseCheck 

# Completed

CleanUp

exit 0;
