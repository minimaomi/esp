#####################################################
#Create external table from pass col1,col2,col4...
# $1 For DBNAME (Needed)
# $2 For pipe name (Needed)
# $3 For select statements (Needed)
# $4 For num of pipes 
# $5 For pdq value
#
# By zhengxm  
# Date 2017-12-11
# Copyright Vsettan 2017-12
#####################################################


. ~informix/.bash_profile

dbname="$1"
tmp_tab="$2"
sql="$3"

trap "sh /tmp/clearsplit.sh $tmp_tab ;exit" 15 2

if [ "$#" -lt 3 ];then
echo "Critical Parameter is null ,not permit,program exit" 
exit
fi
if [ "$4" -le 1 ];then
	num_pipe="1"
else
	num_pipe="%r(1..${4})"
fi

if [ "$5" -lt 1 ];then
	pdq="0"
else
	pdq="$5"
fi
#Get tabname,only one table support now
t_tab=`echo "$sql"|tr '[A-Z]' '[a-z]'|awk -F' from ' '{print $2}'|awk '{print $1}'|sed -e 's/ //g' -e 's/://g' -e 's/;//g'`
echo $t_tab
#t_tab=`echo "$tmp_tab"|awk -F'_' '{print $1}'`

tabname=$dbname.$tmp_tab

#Get tab DDL 
dbschema -d $dbname -t $t_tab -q |grep -vE "^$|revoke|create* index|using btree|{|}" >$tabname 2>/dev/null

#For select * statements 
gen_select_full_ext_tab(){

cat << EOF > ${tabname}_ext.sql
CREATE EXTERNAL TABLE ext_${tmp_tab} SAMEAS $t_tab USING(
DATAFILES("PIPE:/tmp/pipe_${tmp_tab}${num_pipe}.p"),
FORMAT 'informix');
EOF
}
#End gen_select_full_ext_tab

#For select col,col2,col3
gen_selectcol_ext_tab(){

#Get external table columns 

#For select col1,col2 statements and select skip xxx first xx  state
v_sql=`echo "$sql"|grep -cwi "select skip"`
if [ "$v_sql" -eq 1 ];then
	echo "$sql" |tr '[A-Z]' '[a-z]'|awk  '{print $6}' |sed -e 's/,/\n/g' >t_col.${tmp_tab}
	norm=`echo "$sql" |awk  '{print "\\\"$6}'`
else
	echo "$sql" |tr '[A-Z]' '[a-z]'|awk -F' from ' '{print substr($1,8)}' |sed -e 's/,/\n/g' >t_col.${tmp_tab}
	norm=`echo "$sql" |awk  '{print "\\\"$2}'`
fi
echo "#" >>t_col.${tmp_tab}

#Get columns types and length
i=0
#echo "    rrowid integer," >t_${tmp_tab}.ext.sql
for col in `cat t_col.${tmp_tab}`
do
	if [ "$col"x = "#"x ];then
	  sed -e "$i s/,$//" t_${tmp_tab}.ext.sql >${tmp_tab}_ext.sql
	  echo ")" >>${tmp_tab}_ext.sql
	  echo "USING(DATAFILES('PIPE:/tmp/pipe_${tmp_tab}${num_pipe}.p'),FORMAT 'informix');">>${tmp_tab}_ext.sql
	  break;
	fi
    grep -w $col $tabname >>t_${tmp_tab}.ext.sql
    i=$(($i+1))
done

#Gernate external table create statement
echo "CREATE EXTERNAL TABLE ext_${tmp_tab} (" >${tabname}_ext.sql
cat ${tmp_tab}_ext.sql >>${tabname}_ext.sql

#Clear temp sql file
rm -f t_${tmp_tab}.ext.sql t_col.${tmp_tab} ${tmp_tab}_ext.sql 

}
#End gen_selectcol_ext_tab

#Main 
if [ "$norm"x = "\*"x ];then
	gen_select_full_ext_tab 
else
	gen_selectcol_ext_tab 
fi

#Create pipe

for i in {1..20}
do
 mkfifo /tmp/pipe_${tmp_tab}$i.p
done
chown informix:informix /tmp/pipe_${tmp_tab}*.p
chmod 777 /tmp/pipe_${tmp_tab}*.p

exec 4<>/tmp/pipe_${tmp_tab}1.p
exec 5<>/tmp/pipe_${tmp_tab}2.p
exec 6<>/tmp/pipe_${tmp_tab}3.p
exec 7<>/tmp/pipe_${tmp_tab}4.p
exec 8<>/tmp/pipe_${tmp_tab}5.p
exec 9<>/tmp/pipe_${tmp_tab}6.p
exec 10<>/tmp/pipe_${tmp_tab}7.p
exec 11<>/tmp/pipe_${tmp_tab}8.p
exec 12<>/tmp/pipe_${tmp_tab}9.p
exec 13<>/tmp/pipe_${tmp_tab}10.p
exec 14<>/tmp/pipe_${tmp_tab}11.p
exec 15<>/tmp/pipe_${tmp_tab}12.p
exec 16<>/tmp/pipe_${tmp_tab}13.p
exec 17<>/tmp/pipe_${tmp_tab}14.p
exec 18<>/tmp/pipe_${tmp_tab}15.p
exec 19<>/tmp/pipe_${tmp_tab}16.p
exec 20<>/tmp/pipe_${tmp_tab}17.p
exec 21<>/tmp/pipe_${tmp_tab}18.p
exec 22<>/tmp/pipe_${tmp_tab}19.p
exec 23<>/tmp/pipe_${tmp_tab}20.p

#Create exteranl table
dbaccess $dbname ${tabname}_ext.sql >/dev/null 2>&1 

#Check external table create Success
dbschema -d $dbname -t ext_${tmp_tab} 

#Function for  load data
load_dat(){
#echo "set pdqpriority ${pdq};insert into ext_${tmp_tab} $sql " #|dbaccess $dbname  >/dev/null 2>&1 
echo "set pdqpriority ${pdq};insert into ext_${tmp_tab} $sql " |dbaccess $dbname  >/dev/null 2>&1 


#Drop ext tab,clear pipe 
echo "drop table ext_${tmp_tab};"|dbaccess $dbname >/dev/null 2>&1
rm -f /tmp/pipe_${tmp_tab}?.p
rm -f /tmp/pipe_${tmp_tab}??.p
rm -f ${tabname}_ext.sql
rm -f ${tabname}

}
#Begin load data 
load_dat &
