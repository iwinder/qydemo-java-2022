<template>
    <div class="card mb-4 shadow-sm course">
        <img class="img-fluid" v-bind:src="course.image"  @error="defaultImageFuc">
        <div class="card-body">
            <h4 class="">{{course.name}}</h4>
            <p class="card-text">{{course.summary}}</p>
            <div class="d-flex justify-content-between align-items-center">
                <div class="btn-group">
                     <router-link v-bind:to="'/detail?id=' + course.id" class="btn btn-outline-secondary">课程详情</router-link>
                </div>
                <small class="text-muted">
                    <span class="badge badge-info"><i class="fa fa-yen" aria-hidden="true"></i>&nbsp;{{course.price}}</span>&nbsp;
                    <span class="badge badge-info"><i class="fa fa-user" aria-hidden="true"></i>&nbsp;{{course.enroll}}</span>&nbsp;
                    <span class="badge badge-info">{{COURSE_LEVEL | optionKV(course.level)}}</span>  
                </small>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'the-course',
    props: {
        course: {}
    },
    data: function() {
        return {
            COURSE_LEVEL: COURSE_LEVEL
        };
    },
    methods: {
        defaultImageFuc(event) {
            var img=event.srcElement;
            img.src = "../../static/images/course/avatar"+Tool.randomNum(1,8)+".jpg";
            img.onerror = null; //防止一直跳动
        }
    }
}
</script>

<style >
  .course h4 {
    font-size: 1.25rem;
    margin: 15px 0;
  }

  .course .text-muted .badge{
    font-size: 1rem;
  }
</style>