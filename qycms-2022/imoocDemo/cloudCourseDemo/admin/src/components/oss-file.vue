<template>
    <div>
      <button type="button" v-on:click="selectFile()" class="btn btn-white btn-default btn-round">
          <i class="ace-icon fa fa-upload"></i>{{text}}
      </button>
      <input class="hidden" type="file" ref="file" v-on:change="uploadFile()" v-bind:id="inputId+'-input'">
     
    </div>
</template>
<script>
export default {
    name: 'bug-file',
    props: {
      text: {
        default: '上传文件'
      },
      inputId: {
        default: 'file-upload'
      },
      suffixs: {
        default: []
      },
      afterUpload: {
        type: Function,
        default: null
      },
      use: {
          default: ""
      },
    },
    data: function () {
      return {
        
      }
    },
    methods: {
      /**
      * 点击【上传】
      */
      selectFile () {
        let _this = this;
        $("#" + _this.inputId + "-input").trigger("click");
      },
      /**
       * 上传文件
       */
      uploadFile() {
        let _this = this;
        let formData = new window.FormData();
        let file = _this.$refs.file.files[0];
        console.log(file);


        // 生成文件标识，标识多次上传的是不是同一个文件
        let key = hex_md5(file.name + file.size + file.type);
        let key10 = parseInt(key, 16);
        let key62 = Tool._10to62(key10);
        console.log(key, key10, key62);

        // 判断文件格式
        let suffixs = _this.suffixs;
        let fileName = file.name;
        let suffix = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length).toLowerCase();
        if(!(!suffixs || JSON.stringify(suffixs) === "{}" || suffixs.length === 0)) {
            let validateSuffix = false;
            for(let s of suffixs) {
                if(s.toLocaleLowerCase() === suffix) {
                    validateSuffix = true;
                    break;
                }
            }
            if(!validateSuffix) {
                Toast.warning("文件格式不正确！只支持上传：" + suffixs.join(","));
                $("#" + _this0.inputId + "-input").val("");
                return;
            }
        }

        // 文件分片
        let shardSize = 10 * 1024 *1024; // 20M为一个分片
        let shardIndex = 1;   // 分片索引，1表示第1个分片
        let size = file.size;
        let shardTotal = Math.ceil(size / shardSize); // 总分片数
 
        let param = {
          'shardIndex': shardIndex,
          'shardSize': shardSize,
          'shardTotal': shardTotal,
          'use': _this.use,
          'name': file.name,
          'suffix': suffix,
          'size': file.size,
          'key': key62
        };
        _this.check(param);
        // _this.upload(param);
        
      },
      check(param) {
        let _this = this;
        _this.$ajax.get(process.env.VUE_APP_SERVER + "/file/admin/check/" + param.key).then((res)=> {
            let resp = res.data;
            if(resp.success) {
              let obj = resp.content;
              if(!obj) {
                param.shardIndex = 1;
                console.log("没有找到文件记录，从分片1开始上传");
                _this.upload(param);
              } else if (obj.shardIndex === obj.shardTotal) {
                // 已上传分片 = 分片总数，说明已全部上传完，不需要再上传
                Toast.success("文件极速秒传成功！");
                _this.afterUpload(resp);  
                $("#" + _this.inputId + "-input").val("");  
              }else {
                param.shardIndex = obj.shardIndex + 1;
                console.log("没有找到文件记录，从分片1开始上传");
                _this.upload(param);
              }
            } else {
              console.log("文件上传失败");
              $("#" + _this.inputId + "-input").val("");
            }
        });
      },
      /**
       * 递归上传分片
       */
      upload(param) {
        let _this = this;
        let shardIndex = param.shardIndex;
        let shardTotal = param.shardTotal;
        let shardSize = param.shardSize;
        let fileShard = _this.getFileShard(shardIndex,shardSize);


        // 将文件转为base64进行传输
        let fileReader = new FileReader();
        fileReader.readAsDataURL(fileShard);
        Progress.show(parseInt((shardIndex - 1) * 100 / shardTotal));
        fileReader.onload = function (e) {
          let base64 = e.target.result;
          param.shard = base64;
          // Loading.show();
          _this.$ajax.post(process.env.VUE_APP_SERVER + "/file/admin/oss/oss-append", param).then((res)=> {
              // Loading.hide();
              let resp = res.data;  
              Progress.show(parseInt(shardIndex * 100 / shardTotal));
              console.log("文件上传的结果：", resp);
              if(shardIndex < shardTotal) {
                // 上传下一个分片
                param.shardIndex = param.shardIndex + 1;
                _this.upload(param);
              } else {
                 Progress.hide();
                _this.afterUpload(resp);  
                $("#" + _this.inputId + "-input").val("");
              }
          });
        };


      },

      getFileShard(shardIndex, shardSize) {
        let _this = this;
        let file = _this.$refs.file.files[0];
        let start = (shardIndex - 1) * shardSize; // 当前分片起始位置
        let end = Math.min(file.size, start + shardSize); // 当前分片结束位置
        let fileShard = file.slice(start, end); // 从文件中截取当前的分片数据
        return fileShard;
      },
      
    }
}
</script>

<style scoped>
 
</style>