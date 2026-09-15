#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.2
#
# Required ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# ------------------
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

set -e

if [ -z "$MAVEN_SKIP_RC" ]; then
  if [ -f /etc/mavenrc ]; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ]; then
    . "$HOME/.mavenrc"
  fi
fi

# OS specific support.
cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
  Darwin*) darwin=true
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        JAVA_HOME="$(/usr/libexec/java_home)"; export JAVA_HOME
      else
        JAVA_HOME="/Library/Java/Home"; export JAVA_HOME
      fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="$(which javac 2>/dev/null || true)"
  if [ -n "$javaExecutable" ] && ! [ "$(expr "\"$javaExecutable\"" : '\([^ ]*\)')" = "no" ]; then
    readLink=$(which readlink)
    if [ ! $(expr "$readLink" : '\([^ ]*\)') = "no" ]; then
      if $darwin ; then
        javaHome="$(dirname "\"$javaExecutable\"")"
        javaExecutable="$(cd "\"$javaHome\"" && pwd -P)/javac"
      else
        javaExecutable="$(readlink -f "\"$javaExecutable\"")"
      fi
      javaHome="$(dirname "\"$javaExecutable\"")"
      javaHome=$(expr "$javaHome" : '\(.*\)/bin')
      JAVA_HOME="$javaHome"
      export JAVA_HOME
    fi
  fi
fi

if [ -z "$JAVACMD" ]; then
  if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="$(
      \unset -f command 2>/dev/null
      \command -v java
    )"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

# traverses directory structure from process work directory to filesystem root
# first directory with .mvn subdirectory is considered project base directory
find_maven_basedir() {
  if [ -z "$1" ]; then
    echo "Path not specified to find_maven_basedir"
    return 1
  fi

  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ]; do
    if [ -d "$wdir"/.mvn ]; then
      basedir=$wdir
      break
    fi
    # workaround for JBEAP-8937 (on Solaris 10/Sparc)
    if [ -d "${wdir}" ]; then
      wdir=$(
        cd "$wdir/.."
        pwd
      )
    fi
    # end of workaround
  done
  printf '%s' "$(cd "$basedir" && pwd)"
}

# concatenates all lines of a file
concat_lines() {
  if [ -f "$1" ]; then
    # Remove \r in case we run on Windows within Git Bash
    # and check out the repository with auto CRLF management
    # enabled. Otherwise, we may read lines that are delimited with
    # \r\n and produce $'-Xarg\r' rather than -Xarg due to word
    # splitting rules.
    tr -s '\r\n' ' ' < "$1"
  fi
}

log() {
  if [ "$MVNW_VERBOSE" = true ]; then
    printf '%s\n' "$1"
  fi
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")
if [ -z "$BASE_DIR" ]; then
  exit 1
fi

MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
export MAVEN_PROJECTBASEDIR
log "$MAVEN_PROJECTBASEDIR"

##########################################################################################
# Extension to allow automatically downloading the maven-wrapper.jar from Maven Central
# This allows using the maven wrapper in projects that prohibit checking in binary data.
##########################################################################################
wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ -r "$wrapperJarPath" ]; then
  log "Found $wrapperJarPath"
else
  log "Couldn't find $wrapperJarPath, downloading it ..."

  wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  while IFS="=" read -r key value; do
    case "$key" in wrapperUrl)
      wrapperUrl="$value"
      break
      ;;
    esac
  done < "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
  log "Downloading from: $wrapperUrl"

  if command -v wget > /dev/null; then
    wget "$wrapperUrl" -O "$wrapperJarPath" || (rm -f "$wrapperJarPath" && exit 1)
  elif command -v curl > /dev/null; then
    curl -o "$wrapperJarPath" "$wrapperUrl" -f || (rm -f "$wrapperJarPath" && exit 1)
  else
    echo "Could not find wget or curl, please install one to download the Maven Wrapper jar." >&2
    exit 1
  fi
fi
##########################################################################################
# End of extension
##########################################################################################

MAVEN_OPTS="$(concat_lines "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config") $MAVEN_OPTS"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --path --windows "$JAVA_HOME")
  [ -n "$CLASSPATH" ] && CLASSPATH=$(cygpath --path --windows "$CLASSPATH")
  [ -n "$MAVEN_PROJECTBASEDIR" ] && MAVEN_PROJECTBASEDIR=$(cygpath --path --windows "$MAVEN_PROJECTBASEDIR")
fi

exec "$JAVACMD" \
  $MAVEN_OPTS \
  $MAVEN_DEBUG_OPTS \
  -classpath "$wrapperJarPath" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
