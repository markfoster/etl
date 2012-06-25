#!/bin/bash
#
# ETL - Copy Logs to the Filer
#
# Usage: CMS0294CopyLogs.sh  [ -DvtAN ] LogFile names
#        CMS0294CopyLogs.sh [ -DvN ] [ [ -f filename ] | [ -F file list ] ]
#
#    -D: Debug
#    -v: Verbose
#    -t: Today's logs
#    -A: All logs
#    -N: No compression
#    -f: Copy the logs listed in the file (full path names)
#    -F: Copy the logs listed on the command line
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   05/06/12	01.00	MF	Initial version
#

#
# Usage functions
#

Usage ()
{
    echo "usage: CMS0294CopyLogs.sh [ -DvtA ] LogFile names" 1>&2
    echo "       CMS0294CopyLogs.sh [ -Dv ] [ [ -f filename ] | [ -F file list ] ]" 1>&2
    exit 1
}

ErrorExit ()
{
    echo "CMS0294CopyLogs.sh:" "$*" 1>&2;
    exit 1
}

#
# Copy File
#
# 1 = File Name
# 2 = N_FILE

CopyFile ()
{
    fMTStamp=$(stat -c %y "${1}")
    fMDate=$(echo "${fMTStamp}" | cut -c 1-10)
    fMTime=$(echo "${fMTStamp}" | cut -c 12-19)
    FD="${FILE_D}/${2}/${fMDate}"

    mkdir -p "${FD}" 2>/dev/null

    if   [ "${v_FLAG}" = "YES" ]
    then cp -pv "${1}" "${FD}/${H_Name}@${fMTime}.log"
    else cp -p "${1}" "${FD}/${H_Name}@${fMTime}.log"
    fi

# Compress

    if   [ "${N_FLAG}" != "YES" ]
    then rm -f "${FD}/${H_Name}@${fMTime}.log.gz"
         gzip -9 "${FD}/${H_Name}@${fMTime}.log"
    fi
}

#
# Find our commands
#

CMD_PATH=$(dirname "${0}")

if   [ "${CMD_PATH}" = "." ]
then CMD_PATH="${PWD}"
elif [ "${CMD_PATH}" = ".." ]
then CMD_PATH="${PWD}/.."
fi

# Load the environment parameters

if   [ -f "${CMD_PATH}/CMS0260Environment.sh" ]
then . "${CMD_PATH}/CMS0260Environment.sh"
elif [ -f "/usr/local/bin/CMS0260Environment.sh" ]
then . "/usr/local/bin/CMS0260Environment.sh"
else ErrorExit "Missing environment file."
fi

# Check priviledges

if   [ "$(id -u)" -ne 0 ]
then ErrorExit "You do not have sufficient privileges to use this option."
fi

# Initialise

ALL_LOGS="nginx_a nginx_e secure messages cron jetty_a jetty_e sar"

D_FLAG=""
v_FLAG=""
t_FLAG=""
A_FLAG=""
f_FLAG=""
F_FLAG=""
N_FLAG=""

H_Name="$(hostname | sed -e 's/\..*//' -e 's/.*/\L&/')"
FILE_D="${HIP_ROOT}/hip_logs"
mkdir -p "${FILE_D}" 2>/dev/null

# Check it exists

if   [ ! -d "${FILE_D}" ]
then ErrorExit "Cannot find/create the archive tree (${FILE_D})."
fi

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294CopyLogs"
mkdir -p "${TMP_DIR}" 2>/dev/null

tdate=$(date +"%F_%T")
LOG_NAME="$(printf 'CopyLogs.%s.%.6d.log' ${tdate} $$)"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='CopyLogs.????-??-??_??:??:??.??????.log.gz'
LOG_MASK0='CopyLogs.????-??-??_??:??:??.??????.log'

# Check Options

while getopts "DtvAf:FN" op
do
    case "${op}" in

    "D" ) D_FLAG="YES" ;;
    "v" ) v_FLAG="YES" ;;
    "t" ) t_FLAG="YES" ;;
    "A" ) A_FLAG="YES" ;;
    "f" ) f_FLAG="${OPTARG}" ;;
    "F" ) F_FLAG="YES" ;;
    "N" ) N_FLAG="YES" ;;
    * ) Usage ;;

    esac
done

shift $((OPTIND - 1))

if   [ '(' "${t_FLAG}" != "" -o "${A_FLAG}" != "" ')' -a	\
       '(' "${f_FLAG}" != "" -o "${F_FLAG}" != "" ')' ]
then ErrorExit "You cannot mix the t/A and f/F flags."
fi

if   [ $# = 0 -a "${A_FLAG}" = "YES" ]
then set ${ALL_LOGS}
elif [ $# = 0 -a "${F_FLAG}" = "YES" ]
then Usage
elif [ $# = 0 -a "${t_FLAG}" = "YES" ]
then Usage
elif [ $# = 0 -a "${f_FLAG}" = "" ]
then Usage
fi

# Save the old log files

if   [ -f "${LOG_FILE}" ]
then gzip -9f "${LOG_FILE}"
fi

# Compress anything else more than 24 hours old

for i in $(find "${TMP_DIR}" -type f -name "${LOG_MASK0}" -mmin +1440)
do
    gzip -9f "${i}"
done

# Start off

if   [ "${D_FLAG}" = "" ]
then exec >"${LOG_FILE}" 2>&1
fi

echo "Starting Log Copy at $(date)"

dt="$(stat -f -c '%T' "${FILE_D}")"

# If the filer is not mounted - send an alert

if   [ "${dt}" != "nfs" -a "${H_Name}" != "evl3300674" ]
then MAIL_FROM="CMS0294CopyLogs@$(hostname)"

     (
	 echo "To: ${EVENT_MAIL}"
	 echo "From: ${MAIL_FROM}"
	 echo "Subject: ALERT: CMS0294CopyLogs.sh - Filer not mounted"
	 echo

         echo "Central location (${FILE_D}) is not shared [$dt}"
	 stat -f "${FILE_D}"

	 echo
     ) | /usr/lib/sendmail -f "${MAIL_FROM}" -t

     stat -f "${FILE_D}"
     ErrorExit "Central location (${FILE_D}) is not shared [$dt}".
fi

# Handle the F/f flags

if   [ "${F_FLAG}" != "" -o "${f_FLAG}" != "" ]
then (
	if   [ "${F_FLAG}" = "YES" ]
	then for i
	     do
	         echo $i
	     done
	fi

	if   [ "${f_FLAG}" != "" ]
	then cat  "${f_FLAG}"
        fi
     )	 			|
       while read a
       do
             N_TYPE=""

             case "$a" in

	     /opt/jetty/logs/*.access.log) 		N_TYPE="JETTY_access";;
	     /opt/jetty/logs/*.stderrout.log)		N_TYPE="JETTY_error";;
	     /opt/jetty/logs/*.stderrout.log.[0-9]*)	N_TYPE="JETTY_error";;
	     /opt/nginx/logs/access.log*)		N_TYPE="NG_access";;
	     /opt/nginx/logs/error.log*)		N_TYPE="NG_error";;
	     /var/log/cron*)				N_TYPE="LX_cron";;
	     /var/log/messages*)			N_TYPE="LX_messages";;
	     /var/log/sa/sa[0-9][0-9])			N_TYPE="LX_sar";;
	     /var/log/secure*)				N_TYPE="LX_secure";;

	      esac

      if   [ "${N_TYPE}" = "" ]
      then PrintInfo "File [$a] - Unknown type"
      else CopyFile "$a" "${N_TYPE}"
      fi
  done

# Today's or yesterday's?

else
     while [ $# != 0 ]
     do
         PrintInfo "Processing Log [$1]"

         S_FILE=""				# Source file name without extension
         E_FILE=""				# Extension of the file. * is a special case.
         N_FILE=""				# Name of the log directory on the filer.

         case "${1}" in

         "nginx_a")  S_FILE="/opt/nginx/logs/access.log"
		     N_FILE="NG_access"
		     E_FILE=".1" ;;

         "nginx_e")  S_FILE="/opt/nginx/logs/error.log"
		     N_FILE="NG_error"
		     E_FILE=".1" ;;

         "secure")   S_FILE="/var/log/secure"
		     N_FILE="LX_secure"
		     E_FILE=".1" ;;

         "messages") S_FILE="/var/log/messages"
		     N_FILE="LX_messages"
		     E_FILE=".1" ;;

         "cron")     S_FILE="/var/log/cron"
		     N_FILE="LX_cron"
		     E_FILE=".1" ;;

         "jetty_e")  if [ "${t_FLAG}" = "YES" ]
		     then S_FILE="/opt/jetty/logs/$(date +'%Y_%m_%d').stderrout.log"
		     else S_FILE="/opt/jetty/logs/$(date -dyesterday +'%Y_%m_%d').stderrout.log"
		     fi

		     N_FILE="JETTY_error"
		     E_FILE="*" ;;

         "jetty_a")  if [ "${t_FLAG}" = "YES" ]
		     then S_FILE="/opt/jetty/logs/access.log"
		     else S_FILE="/opt/jetty/logs/$(date -dyesterday +'%Y_%m_%d').access.log"

# Need some special processing if jetty is down.

		          if   [ ! -f "${S_FILE}" -a -f "/opt/jetty/logs/access.log" ]
		          then ftime=$(stat -c '%Y' /opt/jetty/logs/access.log)
			       ctime=$(date '+%s')
			       ctime=$((ctime - 600))

			       if   [ "${ftime}" -lt "${ctime}" ]
			       then PrintInfo "No yesterday's file, and today's file has not been access to 10 minutes - save today's file"

			            S_FILE="/opt/jetty/logs/access.log"
			       fi
		          fi
		     fi

		     N_FILE="JETTY_access"
		     E_FILE="" ;;

         "sar")      if [ "${t_FLAG}" = "YES" ]
		     then S_FILE="/var/log/sa/sa$(date +'%d')"
		     else S_FILE="/var/log/sa/sa$(date -dyesterday +'%d')"
		     fi

		     N_FILE="LX_sar"
		     E_FILE="" ;;

         *)	     PrintInfo "Log ID <${1}> - does not exist"
		     ;;
         esac

         if   [ "${S_FILE}" == "" ]
         then echo >/dev/null

         elif [ "${E_FILE}" = '*' ]
         then
              for i in  "${S_FILE}"*
	      do
	          if   [ ! -f "${i}" ]
    	          then PrintInfo "No Jetty log files found: <${S_FILE}>"
	          else CopyFile "${i}" "${N_FILE}"
	          fi
	      done


# Get the time stamp on the file, create an appropriate directory and copy it

         elif [ ! -f "${S_FILE}${E_FILE}" -a "${t_FLAG}" = "" ]
         then PrintInfo "File <${S_FILE}${E_FILE}> - does not exist"

         elif [ ! -f "${S_FILE}" -a "${t_FLAG}" = "YES" ]
         then PrintInfo "File <${S_FILE}> - does not exist"

         elif [ "${t_FLAG}" = "YES" ]
         then CopyFile "${S_FILE}" "${N_FILE}"

         else CopyFile "${S_FILE}${E_FILE}" "${N_FILE}"
         fi

# More to next file

         shift
     done
fi

#
# Cleanup old log files
#

for i in $(find "${TMP_DIR}" -type f -name "${LOG_NAME}${LOG_MASK}" -mtime +30)
do
    PrintInfo "Removing old log file: $i"
    rm -f $i
done

echo "Completed Log Copy at $(date)"

exit 0;
