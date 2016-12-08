# Isme

/**
 div 文本内容只显示两行 多余的用...代替
**/
   word-break: break-all;
    text-overflow: ellipsis;
    display: -webkit-box; /** 对象作为伸缩盒子模型显示 **/
    -webkit-box-orient: vertical; /** 设置或检索伸缩盒对象的子元素的排列方式 **/
    -webkit-line-clamp:2; /** 显示的行数 **/
    overflow: hidden;  /** 隐藏超出的内容 **/
