#!/bin/bash
#
# ETL - Unpack file from CQC
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

ErrorExit ()
{
    echo "CMS0294Unpack_main.sh:" "$*" 1>&2;

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
    rm -f "${P_FLAG}" "${TF1}"
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

if   [ -f "${CMD_PATH}/CMS0260Functions.sh" ]
then . "${CMD_PATH}/CMS0260Functions.sh"
elif [ -f "/usr/local/bin/CMS0260Functions.sh" ]
then . "/usr/local/bin/CMS0260Functions.sh"
else ErrorExit "Functions file missing."
fi

# Set up trap!

trap "{ KillAgent; exit 255; }" 1 2 3 9 15

# Priviledge check

if   [ "$(id -u)" -ne 0 ]
then ErrorExit "You do not have sufficient privileges to use this option."
fi

# Initialise
#
# We need the following environment variables

#D_FLAG				# Debug mode
#I_FLAG				# Solr unpack
#W_FLAG				# Website unpack
#S_FLAG				# SHA1 value
#P_FLAG				# Password file
#V_FLAG				# IV value
#n_FLAG				# No decryption
#v_FLAG				# Verbose flag
#z_FLAG				# Compressed flag
#A_FLAG				# Encryption algorithm

#I_FILE				# INPUT file
#HIP_DATA			# Output directory

if   [ "${I_FILE}" = "" -o "${TAR_PROG}" = "" -o "${TMP_DIR}" = "" -o "${HIP_DATA}" = "" ]
then PrintInfo "Environment variable I_FILE=[${I_FILE}]"
     PrintInfo "Environment variable HIP_DATA=[${HIP_DATA}]"
     PrintInfo "Environment variable TAR_PROG=[${TAR_PROG}]"
     PrintInfo "Environment variable TMP_DIR=[${TMP_DIR}]"
     ErrorExit "Mandatory environment variable not defined"
fi

# Set up log names, directories and masks

TF1="${TMP_DIR}/Pipe.senv.$$"

LOG_MASK='Unpack.????-??-??_??:??:??.log.gz'

# Get the message

Message=""
p=""

# Get text string

if   [ "${W_FLAG}" = "YES" ]
then p="Web Site"
fi

if   [ "${I_FLAG}" = "YES" ]
then Message="${p}"
     p="Solr Indicies"
fi

if   [ "${#Message}" -ne 0 ]
then Message="${Message} and ${p}"
else Message="${p}"
fi

echo "Starting Unpack of H.I.P ${Message} at $(date)"

# Check the SHA1 - only if SHA1 supplied

if   [ "${S_FLAG}" = "" ]
then a=$(/usr/bin/openssl dgst -sha1 -c "${I_FILE}" | sed -e 's/.*)= //')

# Make everything lower case

     a=$(echo "${a}" | tr '[:upper:]' '[:lower:]')
     S_FLAG=$(echo "${S_FLAG}" | tr '[:upper:]' '[:lower:]')

     if   [ "${a}" != "${S_FLAG}" ]
     then PrintVerboseInfo "Calculated SHA1= [${a}]"
          PrintVerboseInfo "Actual SHA1=     [${S_FLAG}]"
          ErrorExit "SHA1 Hash total does not matched - expected= [$a]"
     fi
fi

# Begin decrypt

if  [ "${z_FLAG}" = "YES" ]
then TAR_F="${TAR_F} -xz"
else TAR_F="${TAR_F} -x"
fi

if  [ "${v_FLAG}" = "YES" ]
then TAR_F="${TAR_F} -v"
     echo -e "\nExtracted file list=["
fi

if   [ ! -d "${HIP_DATA}" ]
then ErrorExit "Cannot find the data tree (${HIP_DATA})."
fi

cd "${HIP_DATA}"

if   [ "${n_FLAG}" = "" ]
then /usr/bin/openssl enc -d -"${A_FLAG}" -in "${I_FILE}" -pass "file:${P_FLAG}" -iv "${V_FLAG}" 	|
       "${TAR_PROG}" ${TAR_F}

else "${TAR_PROG}" ${TAR_F} -f "${I_FILE}"
fi

if   [ "$?" -ne 0 ]
then if   [ "${D_FLAG}" != "YES" ]
     then
     
# Use sendmail for some reason on HIP servers

	  MAIL_FROM="CMS0294Unpack_main@$(hostname)"

	  (
	      echo "To: ${EVENT_MAIL}"
	      echo "From: ${MAIL_FROM}"
	      echo "Subject: CMS0294Unpack.sh: Decrypt/unpack failed"
	      echo

	      PrintInfo "CMS0294Unpack.sh: Decrypt/unpack failed - log=["
	      cat "${LOG_FILE}"
	      PrintInfo "]"
	      echo
	  ) | /usr/lib/sendmail -f "${MAIL_FROM}" -t
     fi

     ErrorExit "Decrypt/unpack of ${I_FILE} failed"
fi

unset CMS260PID

if  [ "${v_FLAG}" = "YES" ]
then echo -e "]\n"
fi

# Remove the key file

CleanUpTemps

# Set the permissions

find * | while read a
do
    PrintVerboseInfo "Setting permissions on [${a}]"

    if [ -d "$a" ]
    then chown "${USER_ID}" "${a}"
	 chmod 755 "${a}"
    else chown "${USER_ID}" "${a}"
	 chmod 444 "${a}"
    fi
done

#
# Cleanup old log files
#

for i in $(find "${TMP_DIR}" -type f -name "${LOG_MASK}" -mtime +30)
do
    PrintInfo "Removing old log file: $i"
    rm -f $i
done

echo "Completed Unpack of H.I.P ${Message} at $(date)"
exit 0;
