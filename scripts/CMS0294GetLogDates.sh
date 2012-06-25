#!/bin/bash
#
# ETL - Scan logs for start/end date
#
# Usage: CMS0294GetLogDates.sh [ -DvAEs ] [ -F|T YYYYMMDDHHMM ] [ -f file list ] [ list of files ... ]
#
#    -D: Debug
#    -v: Web Site
#    -A: Assume all files are access logs
#    -E: Assume all files are jetty error logs
#    -s: Assume all files are sar logs
#    -f: File list
#
#   Date	Version	Who	Description
#   ====	=======	===	===========
#
#   14/06/12	01.00	MF	Initial version
#
# Usage functions
#

Usage () {
    echo "usage: CMS0294GetLogDates.sh [ -DvAEs ] [ -F|T YYYYMMDDHHMM ] [ -f file list ] [ list of files ... ]" 1>&2

    CleanUp
    exit 1
}

ErrorExit () {
    echo "CMS0294GetLogDates.sh:" "$*" 1>&2;

    CleanUp
    exit 1
}

# Cleanup on exit

CleanUp ()
{
    rm -f "${TF1}"
}

#
# Check a parameter is numeric
#

CheckNumericValue ()
{
    if [ "${1//[0-9]/}" != "" ]
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
else echo "CMS0294GetLogDates.sh: Environment file missing."
     exit 1
fi

# Flags

D_FLAG=""
v_FLAG=""
A_FLAG=""
E_FLAG=""
s_FLAG=""
f_FLAG=""
F_FLAG=""
T_FLAG=""
Tag_FLAG=""

# Set up log names, directories and masks

TMP_DIR="/var/tmp/CMS0294GetLogDates"
mkdir -p "${TMP_DIR}" 2>/dev/null
TF1="${TMP_DIR}/dump.$$"

# Check Options

while getopts "DvAEsf:T:F:" op
do
    case "${op}" in

    "D" ) D_FLAG="YES" ;;
    "v" ) v_FLAG="YES" ;;

    "A" ) A_FLAG="YES"

	  if  [ "${Tag_FLAG}" != "" ]
	  then ErrorExit "Only one of A, E or s options can be specified at a time."
	  fi

	  Tag_FLAG="A"
	  ;;

    "E" ) E_FLAG="YES"

	  if  [ "${Tag_FLAG}" != "" ]
	  then ErrorExit "Only one of A, E or s options can be specified at a time."
	  fi

	  Tag_FLAG="E"
	  ;;

    "s" ) s_FLAG="YES"

	  if  [ "${Tag_FLAG}" != "" ]
	  then ErrorExit "Only one of A, E or s options can be specified at a time."
	  fi

	  Tag_FLAG="s"
	  ;;


    "f" ) f_FLAG="${OPTARG}" ;;

    "F" ) F_FLAG="${OPTARG}"
          CheckNumericValue "${OPTARG}" "From Data/time"

	  if   [ "${#F_FLAG}" -ne 8 -a "${#F_FLAG}" -ne 12 ]
	  then ErrorExit "FROM value must be YYYYMMDD or YYYMMDDHHMM."
	  fi
	  ;;

    "T" ) T_FLAG="${OPTARG}"
          CheckNumericValue "${OPTARG}" "From Data/time"

	  if   [ "${#T_FLAG}" -ne 8 -a "${#T_FLAG}" -ne 12 ]
	  then ErrorExit "TO value must be YYYYMMDD or YYYMMDDHHMM."
	  fi
	  ;;

    * ) Usage ;;

    esac
done

shift $((OPTIND - 1))

if   [ $# -eq 0 -a "${f_FLAG}" = "" ]
then Usage
fi

# Set up some patterns

GP1='^[0-9]*-[A-Z][a-z]*-[0-9]* [0-9][0-9]:[0-9][0-9]:[0-9][0-9]'
GP2='^[0-9]*-[0-9]*-[0-9]* [0-9][0-9]:[0-9][0-9]:[0-9][0-9]'
SP1='s!^\([0-9]*\)-\([A-Z][a-z]*\)-\([0-9]*\) \([0-9][0-9]\):\([0-9][0-9]\):\([0-9][0-9]\).*!\4:\5 \1/\2/\3!'
SP2='s!^\([0-9]*\)-\([0-9]*\)-\([0-9]*\) \([0-9][0-9]\):\([0-9][0-9]\):\([0-9][0-9]\).*!\4:\5 \3/\2/\1!'
SP3='s!^[^;]*;[^;]*;\(....\)-\(..\)-\(..\) \(..\)-\(..\)-\(..\).*!\4:\5 \3/\2/\1!'

# Begin

(
    if   [ "${f_FLAG}" != "" ]
    then cat "${f_FLAG}"
    fi

    if   [ $# -ne 0 ]
    then for i
	 do
	     echo $i
	 done
    fi
)							|
  while read filename
  do
	ft=$(file -bi "${filename}")

	if  [ "${ft}" = "application/x-gzip" ]
	then CatCmd="zcat"
	else CatCmd="cat"
	fi

	(
	    if   [ "${A_FLAG}" = "YES" -o "${filename/\/NG_access\///}" != "${filename}" -o	\
		   "${filename/\/JETTY_access\///}" != "${filename}" ]
	    then (
		      ${CatCmd} "${filename}" | head -1 
		      ${CatCmd} "${filename}" | tail -1
	         )										|
		    ReadApacheLogs -A -s' ' -f6 		 				|
		    sed -e 's!\([^:]*\):\(.*\):..!\2 \1!'
		  
	    elif [ "${E_FLAG}" = "YES" -o "${filename/\/JETTY_error\///}" != "${filename}" ]
	    then (
	             ${CatCmd} "${filename}" | grep -m 1 -e "${GP1}" -e "${GP2}" 
	             ${CatCmd} "${filename}" | grep -e "${GP1}" -e "${GP2}" | tail -1
                 ) 										|
	           sed -e "${SP1}" -e "${SP2}"						\
		       -e "s!/Jan/!/01/!g" -e "s!/Feb/!/02/!g" -e "s!/Mar/!/03/!g"	\
		       -e "s!/Apr/!/04/!g" -e "s!/May/!/05/!g" -e "s!/Jun/!/06/!g"	\
		       -e "s!/Jul/!/07/!g" -e "s!/Aug/!/08/!g" -e "s!/Sep/!/09/!g"	\
		       -e "s!/Oct/!/10/!g" -e "s!/Nov/!/11/!g" -e "s!/Dec/!/12/!g"

	    elif [ "${s_FLAG}" = "YES" -o "${filename/\/LX_sar\///}" != "${filename}" ]
	    then ${CatCmd} "${filename}" >"${TF1}"
		 sadf -dt "${TF1}"								|
		   grep -v '^#'									|
	           sed -n -e "${SP3}" -e '1p' -e '$p'

	    else echo "UNKNOWN"
		 echo "FORMAT"
	    fi
	)											|
          (
	      read f
	      read l

	      GOOD="OK"

	      if   [ "${f}" != "UNKNOWN" -a "${F_FLAG}" != "" ]
	      then if [ "${#F_FLAG}" -eq 8 ]
	           then fa=$(echo ${l} | sed -e 's!\(..\):\(..\) \(..\)/\(..\)/\(....\)!\5\4\3!')
	           else fa=$(echo ${l} | sed -e 's!\(..\):\(..\) \(..\)/\(..\)/\(....\)!\5\4\3\1\2!')
		   fi

		   if   [ "${fa}" -lt "${F_FLAG}" ]
		   then GOOD="BAD"
		   fi
	      fi

	      if   [ "${f}" != "UNKNOWN" -a "${T_FLAG}" != "" ]
	      then if [ "${#T_FLAG}" -eq 8 ]
	           then la=$(echo ${f} | sed -e 's!\(..\):\(..\) \(..\)/\(..\)/\(....\)!\5\4\3!')
	           else la=$(echo ${f} | sed -e 's!\(..\):\(..\) \(..\)/\(..\)/\(....\)!\5\4\3\1\2!')
		   fi

		   if   [ "${la}" -gt "${T_FLAG}" ]
		   then GOOD="BAD"
		   fi
	      fi

	      if   [ "${GOOD}" = "OK" ]
	      then printf "%-16s From: %s To: %s\n" "${filename}" "${f}" "${l}"
	      fi
          )
  done

# Completed

CleanUp

exit 0;
