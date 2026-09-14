<script>
export default {
  onLaunch: function () {}
}
</script>

<style>
page {
  background: #f5f6f8;
  font-size: 28rpx;
  color: #303133;
}

/* 通用卡片 */
.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.card-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}

/* 表单 */
.form-item {
  margin-bottom: 28rpx;
}
.form-label {
  font-size: 26rpx;
  color: #606266;
  margin-bottom: 12rpx;
  display: block;
}
.form-input {
  background: #f7f8fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  width: 100%;
  box-sizing: border-box;
  height: 88rpx;
  line-height: 88rpx;
}
/* uni-input 内层真实 input 需显式撑高，否则高度塌陷为 0 无法点击输入 */
.form-input .uni-input-wrapper {
  height: 88rpx;
  line-height: 88rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
}
.form-input .uni-input-input {
  height: 88rpx;
  font-size: 28rpx;
}
.form-input .uni-input-placeholder {
  line-height: 88rpx;
}
/* 多行文本域：uni-textarea 内层同样要撑满 */
.form-input.area {
  height: 180rpx;
  line-height: 1.6;
  padding: 20rpx 24rpx;
}
.form-input.area .uni-textarea-wrapper,
.form-input.area .uni-textarea-textarea {
  height: 100%;
  line-height: 1.6;
}
.picker-box {
  background: #f7f8fa;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  color: #909399;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.picker-val {
  color: #303133;
}

/* 按钮 */
.btn-primary {
  background: #1ab394;
  color: #fff;
  border-radius: 12rpx;
  font-size: 30rpx;
  line-height: 88rpx;
  text-align: center;
}

/* 状态标签 */
.tag {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  color: #fff;
  flex-shrink: 0;
}

/* 空态 */
.empty {
  text-align: center;
  color: #c0c4cc;
  padding: 120rpx 0;
  font-size: 26rpx;
}

/* 列表卡片头尾 */
.row-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.row-title {
  font-size: 29rpx;
  font-weight: 600;
  flex: 1;
  margin-right: 16rpx;
}
.row-sub {
  color: #909399;
  font-size: 24rpx;
  margin-top: 10rpx;
  line-height: 1.6;
}
.row-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f2f3f5;
}
.row-time {
  color: #c0c4cc;
  font-size: 22rpx;
}
.btn-mini {
  border: 1rpx solid #1ab394;
  color: #1ab394;
  border-radius: 8rpx;
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  display: inline-block;
}
.btn-mini-gray {
  border: 1rpx solid #dcdfe6;
  color: #606266;
}
</style>
