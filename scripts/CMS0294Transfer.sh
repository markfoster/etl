#!/bin/bash
#
# ETL - Transfer
#
#    -D: Debug
#    -I: Solr Indices
#    -W: Web Site
#    -v: Web Site
#    -S: Solr Target Directory
#    -K: NGINX Private key
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   05/06/12	01.00	MF	Initial version
#

#
# Usage functions
#

Usage () {
    echo "usage: CMS0294Transfer.sh [ -DWIv ] [ -S Solr version number ] [ -K NGINX Private Key ]" 1>&2

    CleanUp
    exit 1
}

ErrorExit () {
    echo "CMS0294Transfer.sh:" "$*" 1>&2;

    if  [ "${SSH_AGENT_PID}" != "" ]
    then /usr/bin/ssh-agent -k >/dev/null
    fi

    CleanUp
    exit 1
}

CleanUp () {
    rm -f "${K_FLAG}" "${TF}"
}

#
# Kill the ssh-agent process if a signal is received
#

KillAgent () {
    ErrorExit "Signal received - terminating"
}

#
# Send Mail Alert
#
# 1: SOLR or WEB message
#

MailAlert ()
{
    MAIL_FROM="CMS0294Transfer@$(hostname)"

    (
	echo "To: ${EVENT_MAIL}"
	echo "From: ${MAIL_FROM}"

	if   [ "${1}" = "SOLR" ]
	then echo "Subject: CMS0294Transfer Failed on Solr Indices"
	else echo "Subject: CMS0294Transfer Failed on Web Site content"
	fi

	echo

	PrintInfo "Transfer log content= ["
	cat "${LOG_FILE}"
	PrintInfo "]"
	
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
else echo "CMS0294Transfer.sh: Environment file missing."
     exit 1
fi

# Flags

RSYNC_FLAGS="-a --stats --delete --compress"

D_FLAG=""
I_FLAG=""
W_FLAG=""
K_FLAG=""
S_FLAG=""

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294Transfer"
mkdir -p "${TMP_DIR}" 2>/dev/null

TF="${TMP_DIR}/Tfile.$$"

LOG_NAME="Transfer.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"
LOG_MASK='Transfer.????-??-??_??:??:??.log.gz'

# Check Options

while getopts "DIWvK:S:" op
do
    case "${op}" in

    "D" ) D_FLAG="YES" ;;
    "I" ) I_FLAG="YES" ;;
    "W" ) W_FLAG="YES" ;;
    "v" ) RSYNC_FLAGS="${RSYNC_FLAGS} -v" ;;

    "S" ) S_FLAG="${OPTARG}"
          CheckNumericValue "${OPTARG}" "Solr verison number"
	  HIP_NROOT="${HIP_SROOT}${OPTARG}"

	  if   [ "${I_FLAG}" != "YES" ]
	  then ErrorExit "-S directive can only be used with the -I directive"
	  fi
	  ;;

    "K" ) K_FLAG="${OPTARG}"

          if   [ "$(echo "${K_FLAG}" | cut -c1)" != "/" ]
          then K_FLAG="${PWD}/${K_FLAG}"
	  fi

          if   [ ! -f "${K_FLAG}" ]
          then ErrorExit "Key file (${K_FLAG}) does not exist."
	  fi

          trap "{ KillAgent; exit 255; }" 2 3 9 15

          pm="$(stat -c "%a %u %g" "${K_FLAG}")"

          if   [ "${pm}" != "400 $(id -u) $(id -g)" ]
          then ErrorExit "Password file (${K_FLAG}) does have the right permissions."
          fi
          ;;

    * ) Usage ;;

    esac
done

shift $((OPTIND - 1))

if   [ $# -ne 0 -o '(' "${I_FLAG}" = "" -a "${W_FLAG}" = "" ')' -o "${K_FLAG}" = "" ]
then Usage
fi

# Save the old log files

if   [ -f "${LOG_FILE}" ]
then d=$(stat -c '%y' "${LOG_FILE}" | cut -c 1-19 | tr ' ' '_')
     gzip -9f "${LOG_FILE}"
     mv -f "${LOG_FILE}.gz" "${LOG_FILE}.${d}.gz"
fi

# Check the SSH-AGENT process is not running

ps -ef | fgrep ssh-agent | sed -e '/fgrep ssh-agent/d' >"${TF}"

if   [ -s "${TF}" ]
then ErrorExit "SSH-AGENT is current active - please terminated all instances first"
fi

# Start the agent and load it's environment

trap "{ KillAgent; }" 1 2 3 9 15

/usr/bin/ssh-agent >"${TF}"
. "${TF}" >/dev/null

rm -f "${TF}"

# Load the keys

/usr/bin/ssh-add "${K_FLAG}"

if   [ "$?" -ne 0 ]
then KillAgent
fi

# Start off

if   [ "${D_FLAG}" = "" ]
then exec >"${LOG_FILE}" 2>&1
fi

echo "Starting Transfer at $(date)"
stime=$(date '+%s')

OAT_HOST="${OAT_HOST_UID}@${OAT_HOST_NAME}:${OAT_HOST_ROOT}"

if   [ "${I_FLAG}" = "YES" ]
then if   [ "${S_FLAG}" = "" ]
     then rsync ${RSYNC_FLAGS} -e "/usr/bin/ssh -i ${K_FLAG}" "${HIP_SROOT}" "${OAT_HOST}"

# OK - Epublication - need to install in a separate directory 

     else OAT_SOLR="${OAT_HOST_UID}@${OAT_HOST_NAME}:${HIP_NROOT}"

	  /usr/bin/ssh -i "${K_FLAG}" "${OAT_HOST_UID}@${OAT_HOST_NAME}" "mkdir -m 755 ${HIP_NROOT}"

	  if  [ "$?" -ne 0 ]
	  then ErrorExit "Cannot create directory - ${HIP_NROOT}"
	  fi

	  rsync ${RSYNC_FLAGS} -e "/usr/bin/ssh -i ${K_FLAG}" "${HIP_SROOT}"/* "${OAT_SOLR}"
     fi

     if   [ "$?" -ne 0 ]
     then MailAlert SOLR
     fi
fi

if   [ "${W_FLAG}" = "YES" ]
then rsync ${RSYNC_FLAGS} -e "/usr/bin/ssh -i ${K_FLAG}" "${HIP_WROOT}" "${OAT_HOST}" 2>&1

     if   [ "$?" -ne 0 ]
     then MailAlert WEB
     fi
fi

# Terminate the agent and remove the key.

CleanUp

/usr/bin/ssh-agent -k  >/dev/null

#
# Cleanup old log files
#

for i in $(find "${TMP_DIR}" -type f -name "${LOG_MASK}" -mtime +30)
do
    PrintInfo "Removing old log file: $i"
    rm -f $i
done

echo "Completed Transfer at $(date)"

etime=$(date '+%s')
etime=$((etime-stime))
esec=$((etime % 60))
emin=$((etime / 60))

echo -n "Completed Transfer at $(date) - Run time = "
printf "%.2d:%.2d:%.2d\n" "$((emin / 60))" "$((emin % 60))" "${esec}"

exit 0;
