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

subject=""
message=""

while getopts ":s:m:" optname
  do
    case "$optname" in
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

subject=$(echo -e $subject | sed -e "s/^'//g" | sed -e "s/'$//g")
message=$(echo "$message" | sed -e "s/^'//g" | sed -e "s/'$//g")

#recipients="mark.foster@steria.co.uk"
subject="P3 CQC ETL Report - ${subject} - ${DATE}"
cat <<! | /usr/lib/sendmail -t
From:${REPORT_MAIL_FROM}
To:${REPORT_MAIL}
Subject: ${subject}
Priority: Urgent
Importance: high
${message}
!
