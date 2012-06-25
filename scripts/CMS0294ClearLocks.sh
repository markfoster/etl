#!/bin/bash
#
# ETL - Clear all process locks
#
# Usage: CMS0294ClearLocks.sh [ -DFv ] -S Solr Version Number
#
#    -D: Debug
#    -v: Web Site
#    -S: Solr Target Directory
#    -F: First time
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   06/06/12	01.00	MF	Initial version
#

#
# Usage functions
#

Usage () {
    echo "usage: CMS0294ClearLocks.sh [ -DFv ] [ -S Solr vesion number ]" 1>&2

    CleanUp
    exit 1
}

ErrorExit () {
    echo "CMS0294ClearLocks.sh:" "$*" 1>&2;

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
# Dump Jetty Process Info
#

JettyDump () {
    PrintInfo "PID            : ${XPID}"
    PrintInfo "PID_FILE       : ${JETTY_PID}"

    if   [ -f "${JETTY_PID}" ]
    then PrintInfo "PID_FILE Status:" $(ls -l "${JETTY_PID}")
         PrintInfo "PID_FILE Value : [" $(<"${JETTY_PID}") "]"
    else PrintInfo "PID_FILE Status: Not found"
    fi

    PrintInfo "Processes : ["
    ps -ef
    PrintInfo "]"
}


#
# Stop the Jetty Process
#

StopJetty ()
{
    /etc/init.d/jetty stop
    XPID="$(ps -eo pid,cmd | sed -e '/^ *[0-9][0-9]* \/usr\/bin\/java/!d' -e 's/^ *\([0-9][0-9]*\) .*/\1/')"

    if   [ -f "${JETTY_PID}" -o "${XPID}" != "" ]
    then JettyDump
         MailAlert JETTYSTOP
	 ErrorExit "Jetty does not appear to have stopped - pid file exists"
    fi
}

#
# Start the Jetty Process
#

StartJetty ()
{
    /etc/init.d/jetty start
    XPID="$(ps -eo pid,cmd | sed -e '/^ *[0-9][0-9]* \/usr\/bin\/java/!d' -e 's/^ *\([0-9][0-9]*\) .*/\1/')"

    if   [ ! -f "${JETTY_PID}" -o "${XPID}" = "" ]
    then JettyDump
         MailAlert JETTYSTART
	 ErrorExit "Jetty does not appear to have restarted - pid file does not exist"
    fi
}

#
# Is Jetty active
#

ActiveJetty ()
{
    if   [ -s "${JETTY_PID}" ]
    then JETTY_ON="$(ps -p $(<"${JETTY_PID}") -ocmd | fgrep -i jetty)"
    fi
}

#
# Verbose Check on index in use
#

VerboseCheck () {
    if   [ "${v_FLAG}" = "YES" ]
    then PrintInfo "Check correct index in use ["
         ls -ld "${HIP_SROOT}"
	 /usr/bin/stat -c '%A I=%i (D=%d) %h %G %U S=%s %y %n' "${HIP_NROOT}/index"
	 /usr/bin/stat -c '%A I=%i (D=%d) %h %G %U S=%s %y %n' "${HIP_SROOT}/index"
	 echo "COMMAND     PID      USER   FD      TYPE             DEVICE      SIZE       NODE NAME"
	 /usr/sbin/lsof	| fgrep "${HIP_SROOT}"
	 PrintInfo "] - End of status info."
    fi
}

#
# Send Mail Alert
#
# 1: SOLR or WEB message
#

MailAlert ()
{
    MAIL_FROM="CMS0294ClearLocks@$(hostname)"

    (
	echo "To: ${EVENT_MAIL}"
	echo "From: ${MAIL_FROM}"

	case "${1}" in

	"JETTYSTOP" )		echo "Subject: CRITICAL - CMS0294ClearLocks failed to stop JETTY on ${HostName}" ;;
	"JETTYSTART" )		echo "Subject: CRITICAL - CMS0294ClearLocks failed to restart JETTY on ${HostName}" ;;
	"FIRST" )		echo "Subject: CRITICAL - CMS0294ClearLocks failed to rename HIP_SOLR on ${HostName}" ;;
	"LINK" )		echo "Subject: CRITICAL - CMS0294ClearLocks failed to create HIP_SOLR link on ${HostName}" ;;
	"NAME_TIMEOUT" )	echo "Subject: CRITICAL - CMS0294ClearLocks failed to find new HIP_SOLR link on ${HostName}" ;;
	"LOCK_TIMEOUT" )	echo "Subject: CRITICAL - CMS0294ClearLocks failed to get the Jetty lock on ${HostName}" ;;
	"LOCK_CLEAR" )		echo "Subject: CRITICAL - CMS0294ClearLocks failed to clear the Jetty lock on ${HostName}" ;;
	"NO_JETTY" )		echo "Subject: CRITICAL - CMS0294ClearLocks found No Jetty active on Master server ${HostName}" ;;
	"NO_JETTY1" )		echo "Subject: WARNING - CMS0294ClearLocks No Jetty active on server ${HostName}" ;;

	* )			echo "Subject: WARNING - CMS0294ClearLocks unknown mail alert on ${HostName}" ;;

	esac

	echo

        DumpFiles "Switch log content" "${LOG_FILE}"

	if   [ "${LOCKDIR}" != "" ]
	then DumpFiles "Lock file (${LOCKDIR}/owner}) content" "${LOCKDIR}/owner"
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

if   [ -f "${CMD_PATH}/CMS0260Environment.sh" ]
then . "${CMD_PATH}/CMS0260Environment.sh"
elif [ -f "/usr/local/bin/CMS0260Environment.sh" ]
then . "/usr/local/bin/CMS0260Environment.sh"
else echo "CMS0294ClearLocks.sh: Environment file missing."
     exit 1
fi

# Flags

D_FLAG=""
v_FLAG=""
S_FLAG=""
F_FLAG=""

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294ClearLocks"
mkdir -p "${TMP_DIR}" 2>/dev/null

LOG_NAME="Switch.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='Transfer.????-??-??_??:??:??.log.gz'
JETTY_PID="/var/run/jetty.pid"
JETTY_ON=""

# Check Options

while getopts "FDvS:" op
do
    case "${op}" in

    "D" ) D_FLAG="YES" ;;
    "F" ) F_FLAG="YES" ;;
    "v" ) v_FLAG="YES" ;;

    "S" ) S_FLAG="${OPTARG}"
          CheckNumericValue "${OPTARG}" "Solr verison number"
	  HIP_NROOT="${HIP_SROOT}${OPTARG}"
	  ;;

    * ) Usage ;;

    esac
done

shift $((OPTIND - 1))

if   [ $# -ne 0 -o "${S_FLAG}" = "" ]
then Usage
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

echo "Starting Solr Switch at $(date)"

HostName="$(hostname | sed -e 's/\..*//')"
ActiveJetty

# Master process

if   [ "${HostName}" = "evl3300675" ]
then if   [ "${JETTY_ON}" = "" ]
     then MailAlert NO_JETTY
	  ErrorExit "No active Jetty on this host"
     fi

# Lock the process and restart jetty

     LOCKDIR="${HIP_SROOT}.lock"

     if   LockProcess
     then MailAlert LOCK_TIMEOUT
	  ErrorExit "Did not get the Jetty lock"
     fi

     StopJetty

# Do we need to rename hip_solr?

     if  [ "${F_FLAG}" = "YES" ]
     then PrintVerboseInfo "Renaming ${HIP_SROOT} to ${HIP_ROOT}/hip_solr1"
	  mv "${HIP_SROOT}" "${HIP_ROOT}/hip_solr1"

	  if   [ "$?" -ne 0 ]
	  then MailAlert FIRST
	       ErrorExit "Cannot rename ${HIP_SROOT} to ${HIP_ROOT}/hip_solr1"
	  fi
     fi

# Create the link

     PrintVerboseInfo "Creating new link ${HIP_NROOT} to ${HIP_SROOT}"

     ln -sTf "${HIP_NROOT}" "${HIP_SROOT}"

     if   [ "$?" -ne 0 ]
     then MailAlert LINK
	  ErrorExit "Cannot link ${HIP_NROOT} to ${HIP_SROOT}"
     fi

     StartJetty

     sleep 30					# it takes Jetty around 30 seconds to get ready

# Remove the lock

     UnLockProcess
     LOCKDIR=""

     VerboseCheck 

     PrintVerboseInfo "Master [${HostName}] update complete"

#
# Non Master - wait
#

elif [ "${JETTY_ON}" = "" ]
then MailAlert NO_JETTY1

else stime=$(date "+%s")
     stime=$((stime + 600))

# Has the master completed?

     while [ "$(find  "${HIP_SROOT}" -maxdepth 0 -printf "%y > %l > %p\n")" != "l > ${HIP_NROOT} > ${HIP_SROOT}" ]
     do
	   sleep 10
	   etime=$(date "+%s")

	   if  [ "${etime}" -ge "${stime}" ]
	   then MailAlert NAME_TIMEOUT
	        ErrorExit "Did not find new link within 10 minutes"
	   fi
     done

     PrintVerboseInfo "Master update complete detected"

# Now get in and restart Jetty

     stime=$(date "+%s")
     stime=$((stime + 600))

     LOCKDIR="${HIP_SROOT}.lock"

     while LockProcess
     do
	   RanSleep 60 10
	   etime=$(date "+%s")

	   if  [ "${etime}" -ge "${stime}" ]
	   then MailAlert LOCK_TIMEOUT
	        ErrorExit "Did not get the Jetty lock within 10 minutes"
	   fi
     done

     StopJetty
     StartJetty

     sleep 30					# it takes Jetty around 30 seconds to get ready

# Remove the lock

     UnLockProcess
     LOCKDIR=""

     VerboseCheck 

     PrintVerboseInfo "Slave [${HostName}] update complete"
fi

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

echo "Completed Solr Switch at $(date)"

exit 0;
