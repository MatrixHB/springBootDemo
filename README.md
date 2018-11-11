# springBootDemo

github上传代码
1. IDEA --> settings --> github中添加自己的账户，git中test一下
2. VCS --> Import into Version Control --> Create Git Repository 选择本地仓库位置，比如项目根目录下，创建完成后会生成一个.git文件
3. 提交代码时，project上右键，git --> add，其次commit，其次Repository --> push
4. 如果出现push rejected错误，可能因为两个文件夹有不相干内容，则
    在项目文件夹中打开git bash，执行语句$ git pull origin master --allow-unrelated-histories
   然后再push即可
