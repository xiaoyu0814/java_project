FROM 172.16.40.78:1180/base/kong

ADD kong-plugin-http301https /usr/local/share/lua/5.1/kong/plugins/kong-plugin-http301https
RUN cd /usr/local/share/lua/5.1/kong/plugins/kong-plugin-http301https
RUN luarocks make
COPY constants.lua /usr/local/share/lua/5.1/kong/constants.lua

