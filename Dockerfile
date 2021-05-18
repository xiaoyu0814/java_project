##设置基础镜像
FROM base-image

##设置控制台字符集编码
ENV LANG C.UTF-8

##设置docker容器的时间
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

##容器内的目录
ENV HOME /wms
#挂载app dir
RUN mkdir ${HOME}

#COPY or ADD source to Image
ADD pie-wms.jar ${HOME}/pie-wms.jar

WORKDIR ${HOME}
EXPOSE 8080
ENTRYPOINT ["java","-jar","pie-wms.jar"]