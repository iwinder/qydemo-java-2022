<template>
    <div>
        <p>
            <button v-on:click="add()" class="btn btn-white btn-default btn-round">
                <i class="ace-icon fa fa-edit"></i>
                新增
            </button>
            &nbsp;
            <button v-on:click="list()" class="btn btn-white btn-default btn-round">
                <i class="ace-icon fa fa-refresh"></i>
                刷新
            </button>
        </p>
        <!-- ref设置pagination组件别名为 pagination -->
        <pagination ref="pagination" v-bind:list="list" v-bind:itemCount="6"></pagination>
        <div class="row">
            <div v-for="course in courses" :key="course.id" class="col-md-4">
                <div class="thumbnail search-thumbnail">
                    <img v-show="!course.image" class="media-object"  src="/static/images/demo-course.jpg" />
                    <img v-show="course.image" class="media-object" v-bind:src="course.image" />
                    <div class="caption">
                        <div class="clearfix">
                            <span class="pull-right label label-primary info-label">{{COURSE_LEVEL | optionKV(course.level)}}</span>&nbsp;
                            <span class="pull-right label label-primary info-label">{{COURSE_CHARGE | optionKV(course.charge)}}</span>&nbsp;
                            <span class="pull-right label label-primary info-label">{{COURSE_STATUS | optionKV(course.status)}}</span>&nbsp;
                        </div>

                        <h3 class="search-title">
                            <a href="#" class="blue">{{course.name}}</a>
                        </h3>
                        <div v-for="teacher in teachers.filter(t=>{return t.id===course.teacherId})" :key="teacher.id" class="profile-activity clearfix">
                            <div>
                                <img v-show="!teacher.image" class="pull-left" src="/ace/assets/images/avatars/avatar5.png">
                                <img v-show="teacher.image" class="pull-left" v-bind:src="teacher.image">
                                <a class="user" href="#"> {{teacher.name}} </a>
                                <br>
                                {{teacher.position}}
                            </div>
                        </div>
                        <P> <span class="blue bolder bigger-150">{{course.price}}&nbsp;<i class="fa fa-rmb"></i></span>&nbsp;</P>
                        <p>{{course.summary}}</p>
                        <p>
                            <span class="badge badge-info">{{course.id}}</span>&nbsp;
                            <span class="badge badge-info">排序：{{course.sort}}</span>&nbsp;
                            <span class="badge badge-info">{{course.time | formatSecond}}</span>&nbsp;
                        </p>
                        <p>
                            <button v-on:click="toChapter(course)" class="btn btn-white btn-xs btn-info btn-round">
                                <!-- <i class="ace-icon fa fa-pencil bigger-120"></i> -->
                                大章
                            </button>&nbsp;
                            <button v-on:click="toContent(course)" class="btn btn-white btn-xs btn-info btn-round">
                                内容
                            </button>&nbsp;
                            <button v-on:click="openSortModal(course)" class="btn btn-white btn-xs btn-info btn-round">
                                排序
                            </button>&nbsp;
                            <!-- 编辑 -->
                            <button v-on:click="edit(course)" class="btn btn-white btn-xs btn-info btn-round">
                                <!-- <i class="ace-icon fa fa-pencil bigger-120"></i> -->
                                编辑
                            </button>&nbsp;
                            <!-- 删除 -->
                            <button v-on:click="del(course.id)" class="btn btn-white btn-xs btn-warning btn-round">
                                删除
                            </button>
                        </p>
                    </div>
                </div>
            </div>
        </div>
      
        <!-- 表单Modal -->
        <div id="form-modal" class="modal fade"   tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="myModalLabel">表单</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal">
                             <div class="form-group">
                                <label class="col-sm-2 control-label">
                                分类
                                </label>
                                <div class="col-sm-10">
                                <ul id="tree" class="ztree"></ul>
                                </div>
                            </div> 

                            <div class="form-group">
                                <label   class="col-sm-2 control-label">封面</label>
                                <div class="col-sm-10"> 
                                    <big-file v-bind:input-id="'image-upload'" v-bind:text="'上传封面'"  
                                            v-bind:suffixs="['jpg','jpeg','png']" 
                                            v-bind:use="FILE_USE.COURSE.key"
                                            v-bind:after-upload="afterUpload"
                                            ></big-file>
                                     <div v-show="course.image" class="row">
                                            <div class="col-md-4">
                                                <img v-bind:src="course.image" class="img-responsive">
                                            </div>
                                    </div>
                                </div>
                            </div>
 
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">名称</label>
                                <div class="col-sm-10">

                                     <input   v-model="course.name" class="form-control" placeholder="名称">
                                </div>
                            </div>
                              <div class="form-group">
                                <label   class="col-sm-2 control-label">讲师</label>
                                <div class="col-sm-10">
                                    <select v-model="course.teacherId" class="form-control">
                                        <option v-for="o in teachers" :key="o.id" v-bind:value="o.id">{{o.name}}</option>
                                    </select>
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">概述</label>
                                <div class="col-sm-10">
                                    <textarea v-model="course.summary" class="form-control"></textarea>
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">时长</label>
                                <div class="col-sm-10">

                                     <input   v-model="course.time" class="form-control" placeholder="时长">
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">价格（元）</label>
                                <div class="col-sm-10">

                                     <input   v-model="course.price" class="form-control" placeholder="价格（元）">
                                </div>
                            </div>
 
                            <!-- <div class="form-group">
                                <label   class="col-sm-2 control-label">封面</label>
                                <div class="col-sm-10">

                                     <input   v-model="course.image" class="form-control" placeholder="封面">
                                </div>
                            </div> -->
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">级别</label>
                                <div class="col-sm-10">

                                    <select v-model="course.level" class="form-control">
                                        <option v-for="o in COURSE_LEVEL"  :key="o.key" v-bind:value="o.key">{{o.value}}</option>
                                    </select>
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">收费</label>
                                <div class="col-sm-10">

                                    <select v-model="course.charge" class="form-control">
                                        <option v-for="o in COURSE_CHARGE"  :key="o.key" v-bind:value="o.key">{{o.value}}</option>
                                    </select>
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">状态</label>
                                <div class="col-sm-10">

                                    <select v-model="course.status" class="form-control">
                                        <option v-for="o in COURSE_STATUS"  :key="o.key" v-bind:value="o.key">{{o.value}}</option>
                                    </select>
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">报名数</label>
                                <div class="col-sm-10">

                                     <input   v-model="course.enroll" class="form-control" placeholder="报名数">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2 control-label">顺序</label>
                                <div class="col-sm-10">
                                <input v-model="course.sort" class="form-control" disabled>
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <button type="button" v-on:click="save()" class="btn btn-primary">保存</button>
                    </div>
                </div>
            </div>
        </div>

 


        <!-- 排序Modal -->
        <div id="course-sort-modal" class="modal fade" tabindex="-1" role="dialog" style="overflow:auto;">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">排序</h4>
                </div>
                <div class="modal-body"> 
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label class="control-label col-lg-3">
                            当前排序
                            </label>
                            <div class="col-lg-9">
                            <input class="form-control" v-model="sort.oldSort" name="oldSort" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-lg-3">
                            新排序
                            </label>
                            <div class="col-lg-9">
                            <input class="form-control" v-model="sort.newSort" name="newSort">
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-white btn-default btn-round" data-dismiss="modal">
                    <i class="ace-icon fa fa-times"></i>
                    取消
                    </button>
                    <button type="button" class="btn btn-white btn-info btn-round" v-on:click="updateSort()">
                    <i class="ace-icon fa fa-plus blue"></i>
                    更新排序
                    </button>
                </div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div><!-- /.排序 modal -->
    </div>

</template>

<script>

    import Pagination from '../../components/pagination';
    import BigFile from "../../components/big-file";

    export default {
        name: 'business-course',
        components: {Pagination, BigFile},
        data: function() {
            return {
                course: {},
                courses: [],
                COURSE_LEVEL:COURSE_LEVEL,
                COURSE_CHARGE:COURSE_CHARGE,
                COURSE_STATUS:COURSE_STATUS,
                FILE_USE: FILE_USE,
                categorys: {},
                tree: {},
                saveContentLabel:'',
                sort: {
                    id: '',
                    oldSort: 0,
                    newSort: 0,
                },
                teachers: [], 
                
            }
        },
        mounted: function() {
            // this.$parent.activeSidebar("sidebar-business-course");
            let _this = this;
            _this.allCategory();
            _this.allTeacher();
            _this.list(1);
        },
        methods: {
            /**
             * 点击【新增】
             */
            add() {
                let _this = this;
                _this.course = {
                     sort: _this.$refs.pagination.total + 1
                };
                _this.tree.checkAllNodes(false);
                $("#form-modal").modal("show");
            },
            /**
             * 点击【编辑】
             */
            edit(course) {
                let _this = this;
                // _this.course = course;
                // 复制给新对象，防止修改影响到源对象
                _this.course = $.extend({},course);
                _this.listCategory(course.id);
                $("#form-modal").modal("show");
            },
            /**
             * 点击【大章】
             */
            toChapter(course) {
                let _this = this;
                SessionStorage.set(SESSION_KEY_COURSE, course);
                _this.$router.push("/business/chapter");
            },
            /**
             * 点击【保存】
             */
            save() {
                let _this = this;
                // 保存校验
                if (1 != 1
                    || !Validator.require(_this.course.name, "名称")
                    || !Validator.length(_this.course.name, "名称", 1, 50)
                    || !Validator.length(_this.course.summary, "概述", 1, 2000)
                    || !Validator.length(_this.course.image, "封面", 1, 100)
                ) {
                    return;
                }
                let categorys = _this.tree.getCheckedNodes();
                if (Tool.isEmpty(categorys)) {
                    Toast.warning("请选择分类！");
                    return;
                }
                _this.course.categorys = categorys;
                Loading.show();
                _this.$ajax.post(process.env.VUE_APP_SERVER + "/business/admin/course/save",  _this.course).then((response)=>{
                    Loading.hide(_this.$isDebug);
                    console.log("保存课程的结果：", response);
                    let resp = response.data;
                    if(resp.success) {
                        $("#form-modal").modal("hide");
                        _this.list(1);
                        Toast.success("保存成功");
                    }else {
                        Toast.error(resp.message);
                    }


                });
            },
            /**
             * 点击【删除】
             */
            del(courseId) {
                let _this = this;
                Confirm.show('确认删除？',"删除后不可恢复，确认删除？", function(){
                    Loading.show();
                    _this.$ajax.delete(process.env.VUE_APP_SERVER + "/business/admin/course/delete/"+courseId).then((response)=>{
                        Loading.hide(_this.$isDebug);
                        console.log("删除课程列表结果：", response);
                        let resp = response.data;
                        if (resp.success) {
                            _this.list(1);
                            Toast.success("删除成功");

                        }
                    })
                })
            },
            /**
             * 列表查询
             */
            list(page) {
                let _this = this;
                 Loading.show();
                _this.$ajax.post(process.env.VUE_APP_SERVER + "/business/admin/course/list", {
                    page: page,
                    size: _this.$refs.pagination.size // $refs使用组件别名pagination，获取组件里面的变量size
                }).then((response)=>{
                    console.log("查询课程的结果：", response);
                    Loading.hide(_this.$isDebug);
                    let resp = response.data;
                    _this.courses = resp.content.list;
                    // 重新渲染分页组件，使其页码样式与查询页数相同
                    _this.$refs.pagination.render(page, resp.content.total);
                });
            },
            /**
             * 获取课程分类
             */
            allCategory() {
                let _this = this;
                _this.$ajax.post(process.env.VUE_APP_SERVER + "/business/admin/category/all").then((response)=>{
                    console.log("查询分类的结果：", response);
                    let resp = response.data;
                    _this.categorys = resp.content;
                    // 将所有记录格式化成树形结构
                    _this.initTree();
                     
                });
            },
            /**
             * 初始化课程分类树
             */
            initTree() {
                let _this = this;
                let setting = {
                    check: {
                        enable: true
                    },
                    data: {
                        simpleData: {
                            idKey: "id",
                            pIdKey: "parent",
                            rootPId: "00000000",
                        enable: true
                        }
                    }
                };
                let zNodes = _this.categorys;
 
                _this.tree = $.fn.zTree.init($("#tree"), setting, zNodes);
            },
            /**
             * 查找课程下所有分类
             */
            listCategory(courseId) {
                let _this = this;
                Loading.show();
                _this.$ajax.post(process.env.VUE_APP_SERVER + '/business/admin/course/list-category/' + courseId).then((response)=>{
                    Loading.hide();
                    console.log("查询课程下所有分类的结果：", response);
                    let resp = response.data;
                    let categorys = resp.content;
                    // 勾选查询到的分类
                    _this.tree.checkAllNodes(false);
                    for (let i = 0; i < categorys.length; i++) {
                        let node = _this.tree.getNodeByParam("id", categorys[i].categoryId);
                        _this.tree.checkNode(node, true);
                    }
                });
            },
            
            /**
            * 点击【内容】
            */
            toContent(course) {
                let _this = this;
                SessionStorage.set(SESSION_KEY_COURSE, course);
                _this.$router.push("/business/content");
            },
             openSortModal(course) {
                let _this = this;
                _this.sort = {
                    id: course.id,
                    oldSort: course.sort?course.sort:0,
                    newSort: course.sort?course.sort:0
                };
                $("#course-sort-modal").modal("show");
            },

            /**
             * 排序
             */
            updateSort() {
                let _this = this;
                if (_this.sort.newSort === _this.sort.oldSort) {
                    Toast.warning("排序没有变化");
                    return;
                }
                Loading.show();
                _this.$ajax.post(process.env.VUE_APP_SERVER + "/business/admin/course/sort", _this.sort).then((res) => {
                    let response = res.data;

                    if (response.success) {
                        Toast.success("更新排序成功");
                        $("#course-sort-modal").modal("hide");
                        _this.list(1);
                    } else {
                        Toast.error("更新排序失败");
                    }
                });
            },
            /**
             * 获取所有讲师
             */
            allTeacher() {
                let _this = this;
                Loading.show();
                _this.$ajax.post(process.env.VUE_APP_SERVER + '/business/admin/teacher/all').then((response)=>{
                    Loading.hide();
                    let resp = response.data;
                    _this.teachers = resp.content;
                });
            },
            /**
             * 封面上传回调
             */
            afterUpload(response) {
                let _this = this;
                let image = response.content.path;
                _this.course.image = process.env.VUE_APP_FILE_SERVER + image;
           },
 
        }
    }
</script>

<style scoped>
  .caption h3 {
    font-size: 20px;
  }
  @media (max-width: 1199px) {
    .caption h3 {
      font-size: 16px;
     }
   }
</style>