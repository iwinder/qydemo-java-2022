<template>
    <div>
        <h4 class="lighter">
            <i class="ace-icon fa fa-hand-o-right icon-animated-hand-pointer blue"></i>
            <router-link to="/business/course" class="pink"> {{course.name}} </router-link>：
            <i class="ace-icon fa fa-hand-o-right icon-animated-hand-pointer blue"></i>
            <router-link to="/business/chapter" class="pink"> {{chapter.name}} </router-link>
         </h4>
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
        <table id="simple-table" class="table  table-bordered table-hover">
            <thead>
            <tr>
                <th>ID</th>
                <th>标题</th>
                <th>VOD</th>
                <th>时长</th>
                <th>收费</th>
                <th>顺序</th>


                <th>操作</th>
            </tr>
            </thead>

            <tbody>
            <tr v-for="section in sections" :key="section.id" >
                <td>{{section.id}}</td>
                <td>{{section.title}}</td>
                <td>{{section.vod}}</td>
                <td>{{section.time | formatSecond}}</td>
                 <td>{{SECTION_CHARGE | optionKV(section.charge)}}</td>
                <td>{{section.sort}}</td>
                <td> 
                    <button v-on:click="play(section)" class="btn btn-xs btn-info">
                        <i class="ace-icon fa fa-video-camera bigger-120"></i>
                    </button>
                    <!-- 编辑 -->
                    <button v-on:click="edit(section)" class="btn btn-xs btn-info">
                        <i class="ace-icon fa fa-pencil bigger-120"></i>
                    </button>
                    <!-- 删除 -->
                    <button v-on:click="del(section.id)" class="btn btn-xs btn-danger">
                        <i class="ace-icon fa fa-trash-o bigger-120"></i>
                    </button>
                </td>
            </tr> <!--tr结束 -->
            </tbody>
        </table>



        <!-- Modal -->
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
                                <label   class="col-sm-2 control-label">标题</label>
                                <div class="col-sm-10">
                                    <input   v-model="section.title" class="form-control" placeholder="标题">
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">课程</label>
                                <div class="col-sm-10">
                                    <p class="form-control-static">{{course.name}}</p>
                                </div>
                            </div>
 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">大章</label>
                                <div class="col-sm-10">
                                   <p class="form-control-static">{{chapter.name}}</p>
                                </div>
                            </div>

                            <div class="form-group">
                                <label   class="col-sm-2 control-label">视频</label>
                                <div class="col-sm-10"> 
                                    <vod v-bind:input-id="'video-upload'" v-bind:text="'上传大视频'"  
                                            v-bind:suffixs="['mp4']" 
                                            v-bind:use="FILE_USE.COURSE.key"
                                            v-bind:after-upload="afterUpload"
                                            ></vod>
                                     <div v-show="section.video"  class="row">
                                        <div class="col-md-9">
                                            <player ref="player" v-bind:player-id="'form-player-div'" ></player>
                                            <video v-bind:src="section.video"   id="video"  class="hidden" controls="controls"></video>
                                        </div>
                                    </div>
                                </div>
                            </div>

 
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">时长</label>
                                <div class="col-sm-10">
                                    <input   v-model="section.time" class="form-control" placeholder="时长">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2 control-label">视频</label>
                                <div class="col-sm-10">
                                <input v-model="section.video" class="form-control" disabled>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-2 control-label">VOD</label>
                                <div class="col-sm-10">
                                <input v-model="section.vod" class="form-control" disabled>
                                </div>
                            </div>
                            <div class="form-group">
                                <label   class="col-sm-2 control-label">收费</label>
                                <div class="col-sm-10">
                                    <select v-model="section.charge" class="form-control">
                                        <option v-for="o in SECTION_CHARGE" :key="o.key" v-bind:value="o.key">{{o.value}}</option>
                                    </select>                   
                                </div>
                            </div>
                
                            <div class="form-group">
                                <label class="col-sm-2 control-label">顺序</label>
                                <div class="col-sm-10">
                                <input v-model="section.sort" class="form-control">
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

         <modal-player ref="modalPlayer"></modal-player>
    </div>

</template>

<script>

    import Pagination from '../../components/pagination';
    import Vod from '../../components/vod';
    import Player from '../../components/player';
    import ModalPlayer from "../../components/modal-player";

    export default {
        name: 'business-section',
        components: {Pagination, Vod, Player,ModalPlayer},
        data: function() {
            return {
                section: {},
                sections: [],
                SECTION_CHARGE: SECTION_CHARGE,
                course: {},
                chapter: {},
                FILE_USE: FILE_USE,
            }
        },
        mounted: function() {
            this.$parent.activeSidebar("sidebar-business-course");
            let _this = this;
            let course = SessionStorage.get(SESSION_KEY_COURSE) || {};
            let chapter = SessionStorage.get(SESSION_KEY_CHAPTER) || {};
            if (Tool.isEmpty(course) || Tool.isEmpty(chapter)) { 
                 _this.$router.push("/welcome");
            }
            _this.course = course;
            _this.chapter = chapter;
            _this.list(1);
        },
        methods: {
            /**
             * 点击【新增】
             */
            add() {
                let _this = this;
                _this.section = {};
                $("#form-modal").modal("show");
            },
            /**
             * 点击【编辑】
             */
            edit(section) {
                let _this = this;
                // _this.section = section;
                // 复制给新对象，防止修改影响到源对象
                _this.section = $.extend({},section);
                $("#form-modal").modal("show");
            },
            /**
             * 点击【保存】
             */
            save() {
                let _this = this;
                 _this.section.video = "";
                // 保存校验
                if (1 != 1
                    || !Validator.require(_this.section.title, "标题")
                    || !Validator.length(_this.section.title, "标题", 1, 50)
                    || !Validator.length(_this.section.video, "视频", 1, 200)
                ) {
                    return;
                }
                _this.section.courseId = _this.course.id;
                _this.section.chapterId = _this.chapter.id;
                Loading.show();
                _this.$ajax.post(process.env.VUE_APP_SERVER + "/business/admin/section/save",  _this.section).then((response)=>{
                    Loading.hide(_this.$isDebug);
                    console.log("保存小节的结果：", response);
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
            del(sectionId) {
                let _this = this;
                Confirm.show('确认删除？',"删除后不可恢复，确认删除？", function(){
                    Loading.show();
                    _this.$ajax.delete(process.env.VUE_APP_SERVER + "/business/admin/section/delete/"+sectionId).then((response)=>{
                        Loading.hide(_this.$isDebug);
                        console.log("删除小节列表结果：", response);
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
                _this.$ajax.post(process.env.VUE_APP_SERVER + "/business/admin/section/list", {
                    page: page,
                    size: _this.$refs.pagination.size, // $refs使用组件别名pagination，获取组件里面的变量size
                    courseId: _this.course.id,
                    chapterId: _this.chapter.id,
                }).then((response)=>{
                    console.log("查询小节的结果：", response);
                    let resp = response.data;
                    _this.sections = resp.content.list;
                    // 重新渲染分页组件，使其页码样式与查询页数相同
                    _this.$refs.pagination.render(page, resp.content.total);
                });
            },
            afterUpload(resp) {
                let _this = this;
                // let video = process.env.VUE_APP_FILE_SERVER + resp.content.path;
                // let video = process.env.VUE_APP_FILE_OSS + resp.content.path;
                 let video =  resp.content.path;
                 let vod = resp.content.vod;

                // _this.section.video =  video;
                _this.$set(_this.section, 'video', video);
                _this.section.vod = vod;
                    _this.$refs.player.playUrl(video);
                _this.getTime();
            },
            getTime() {
                let _this = this;
                let ele = document.getElementById("video");
                ele.oncanplay = function () { 
                    console.log("ele", ele);
                    console.log("ele", ele.duration); 
                     setTimeout(function () {  
                    // _this.section.time = parseInt(ele.duration, 10);
                        _this.$set(_this.section, 'time', parseInt(ele.duration, 10));
                    }, 1000);
                  
                }
               
            },
            /**
            * 播放视频
            * @param section
            */
            play(section) {
                let _this = this;
                _this.$refs.modalPlayer.playVod(section.vod);
            },
        }
    }
</script>

<style scoped>
  video {
    width: 100%;
    height: auto;
    margin-top: 10px;
  }
</style>