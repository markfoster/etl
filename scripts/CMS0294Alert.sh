#!/bin/bash
#
# CMS0294Alert.sh - Standard alerting feature
#
# $Id: CMS0294Functions.sh,v 1.4 2012/05/25 08:48:46 appadmin Exp $
#
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   03/06/12	01.07	MF	Initial version
#
#

#
# Print/Usage functions
#

PrintInfo ()
{
    echo "$(date '+[%Y-%m-%d %H:%M:%S]') " "$*"
}

PrintVerboseInfo ()
{
    if   [ "${v_FLAG}" = "YES" ]
    then echo "$(date '+[%Y-%m-%d %H:%M:%S]') " "$*"
    fi
}

DATE=`date`
SERVER=`hostname | awk -F. '{print $1}' | tr A-Z a-z`

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
else echo "CMS0294Alert.sh: Environment file missing."
     exit 1
fi

category=0
subject=""
message=""

while getopts ":c:s:m:" optname
  do
    case "$optname" in
      "c")
        category=$OPTARG
        ;;
      "s")
        subject=$OPTARG
        ;;
      "m")
        message=$OPTARG
        ;;
      "?")
        echo "Unknown option $OPTARG"
        ;;
      ":")
        echo "No argument value for option $OPTARG"
        ;;
      *)
      # Should not occur
        echo "Unknown error while processing options"
        ;;
    esac
  done

if [ "$category" == "0" ]; then
  exit -1
fi

# Determine the suject line depending on the category
case "$category" in 
   "1")
     esubject="P1 SEV1 CQC - "
     ;;
   "2")
     esubject="P2 WARN CQC - "
     ;;
   *)
     esubject="P3 INFO CQC - "
     ;;
esac

#recipients="mark.foster@steria.co.uk"
subject="${esubject} ${subject} - ${SERVER} - ${DATE}"
cat <<! | /usr/lib/sendmail -t
From:${MAIL_FROM}
To:${EVENT_MAIL}
Subject: ${subject}
Priority: Urgent
Importance: high
${message}
!
