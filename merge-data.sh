# Merge addition-dictionary/*.txt into dictionary/*.txt
cp -r tmp/OpenCC-master/data/dictionary/* src/main/resources/dictionary/
cp -r tmp/OpenCC-master/data/config/* src/main/resources/config/
git --no-pager diff src/main/resources/dictionary/

for x in src/main/resources/addition-dictionary/*.txt; do
  target="src/main/resources/dictionary/$(basename $x .txt).txt"
  echo "Merging $x to $target"

  cat $x >> $target
done