if [[ ! -z $INSTANCE_FOLDER ]]; then
    rm $INSTANCE_FOLDER/mods/cobblemounts-*.jar
fi
rm build/libs/cobblemounts-*.jar
./gradlew build
if [[ ! -z $INSTANCE_FOLDER ]]; then
    cp build/libs/cobblemounts-*.jar $INSTANCE_FOLDER/mods
fi