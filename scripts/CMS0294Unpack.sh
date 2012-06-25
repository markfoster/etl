#!/bin/bash
#
# ETL - Unpack file from CQC - Front End script
#
# Usage: CMS0294Unpack.sh -NDIWnvz [ -A Algorithm ] [ -V Initialisation Vector ] [ -S SHA1 Hash ]
#			           [ -P Password File ] Encrypted file
#
#    -D: Debug
#    -I: Solr indices
#    -W: Web Pages
#    -V: IV value
#    -S: SHA1 value
#    -P: Password File
#    -A: Encyrption algorithm
#    -n: Tar file is not encrypted
#    -v: Verbose
#    -z: Tar file is compressed
#    -N: Run in background
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
    echo "usage: CMS0294Unpack.sh -DIWnvzN [ -A Algorithm ] [ -V Initialisation Vector ] [ -S SHA1 Hash ]" 1>&2;
    echo "                        [ -P Password File ] Encrypted file" 1>&2;

    rm -f "${P_FLAG}"
    exit 1
}

ErrorExit ()
{
    echo "CMS0294Unpack.sh:" "$*" 1>&2;

    KillAgent
    exit 1
}

#
# Trap exit
#

KillAgent ()
{
    if   [ "${CMS260PID}" != "" ]
    then kill -s INT "${CMS260PID}" 2>/dev/null
    fi

    CleanUpTemps

    exit 1;
}

#
# Clean up temporary stuff
#

CleanUpTemps ()
{
    rm -f "${P_FLAG}"  "${TF1}"
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
else ErrorExit "Environment file missing."
fi

if   [ -f "${CMD_PATH}/tar" ]
then export TAR_PROG="${CMD_PATH}/tar"
else export TAR_PROG=$(which tar)
fi

# Need to ignore Unknown headers in tar archives

if   "${TAR_PROG}" --version | fgrep 1.26 >/dev/null
then export TAR_F="--warning=no-unknown-keyword "
else export TAR_F=""
fi

# Priviledge check

if   [ "$(id -u)" -ne 0 ]
then ErrorExit "You do not have sufficient privileges to use this option."
fi

# Initialise

export D_FLAG=""
export I_FLAG=""
export W_FLAG=""
export S_FLAG=""
export P_FLAG=""
export V_FLAG=""
export n_FLAG=""
export N_FLAG=""
export v_FLAG=""
export z_FLAG=""
export A_FLAG="aes-256-cbc"

# Set up log names, directories and masks

export TMP_DIR="/var/tmp/CMS0294Unpack"
mkdir -p "${TMP_DIR}" 2>/dev/null
TF1="${TMP_DIR}/Pipe.menv.$$"

LOG_NAME="Unpack.log"
LOG_FILE="${TMP_DIR}/${LOG_NAME}"

# Check Options

while getopts "NzvnDIWV:S:P:" op
do
    case "${op}" in

    "D" ) D_FLAG="YES"
          v_FLAG="YES" ;;

    "n" ) n_FLAG="YES" ;;
    "v" ) v_FLAG="YES" ;;
    "z" ) z_FLAG="YES" ;;
    "N" ) N_FLAG="YES" ;;

    "I" ) I_FLAG="YES"
	  export HIP_DATA="${HIP_SROOT}"
	  ;;

    "W" ) W_FLAG="YES"
	  export HIP_DATA="${HIP_WROOT}"
	  ;;

    "A" ) A_FLAG="${OPTARG}"

          if [ "${ENC_OPTIONS/${A_FLAG},/}" = "${ENC_OPTIONS}" ]
          then ErrorExit "Unknown encryption algorithim (${A_FLAG})."
          fi
	  ;;

    "P" ) P_FLAG="${OPTARG}"

          if   [ "$(echo "${P_FLAG}" | cut -c1)" != "/" ]
          then P_FLAG="${PWD}/${P_FLAG}"
	  fi

          if   [ ! -f "${P_FLAG}" ]
          then ErrorExit "Key file (${P_FLAG}) does not exist."
	  fi

          trap "{ rm -f "${P_FLAG}"; exit 255; }" 2 3 9 15

          pm="$(stat -c "%a %u %g" "${P_FLAG}")"

          if   [ "${pm}" != "400 $(id -u) $(id -g)" ]
          then ErrorExit "Password file (${P_FLAG}) does have the right permissions."
          fi
          ;;

    "V" ) V_FLAG="${OPTARG}" ;;
    "S" ) S_FLAG="${OPTARG}" ;;
    * ) Usage ;;
    esac
done

shift $((OPTIND - 1))

# Check parameters supplied

if   [ $# -ne 1 -o '(' "${I_FLAG}" = "" -a "${W_FLAG}" = "" ')' -o					\
       '(' "${n_FLAG}" = "" -a '(' "${V_FLAG}" = "" -o "${S_FLAG}" = "" ')' ')' ]
then Usage
fi

# Check the input file
export I_FILE="${1}"

if   [ "$(echo "${I_FILE}" | cut -c1)" != "/" ]
then I_FILE="${PWD}/${I_FILE}"
fi

if   [ ! -f "${I_FILE}" ]
then ErrorExit "Input file (${I_FILE}) does not exist."
fi

# ask the user for the password if necessary

if   [ "${n_FLAG}" = "" -a "${P_FLAG}" = "" ]
then "${CMD_PATH}/CMS0260PasswordPipe" > "${TF1}"

# Get the filename and PID

     . "${TF1}"
     rm -f "${TF1}"

     if   [ "${CMS260PIPE}" = "" ]
     then ErrorExit "Cannot create link to password manager."
     else P_FLAG="${CMS260PIPE}"
     fi

     trap "{ KillAgent; }" 1 2 3 9 15
fi

# Save the old log files

if   [ -f "${LOG_FILE}" ]
then d=$(stat -c '%y' "${LOG_FILE}" | cut -c 1-19 | tr ' ' '_')
     gzip -9f "${LOG_FILE}"
     mv -f "${LOG_FILE}.gz" "${LOG_FILE}.${d}.gz"
fi

# Start off

if   [ "${N_FLAG}" = "YES" ]
then PrintInfo "Running unpack in background"
     nohup "${CMD_PATH}/CMS0294Unpack_main.sh" >"${LOG_FILE}" 2>&1 &
     exit 0;

elif [ "${D_FLAG}" = "" ]
then exec "${CMD_PATH}/CMS0294Unpack_main.sh" >"${LOG_FILE}" 2>&1

else exec "${CMD_PATH}/CMS0294Unpack_main.sh"
fi

ErrorExit "Failed to start main process"
exit 1;
