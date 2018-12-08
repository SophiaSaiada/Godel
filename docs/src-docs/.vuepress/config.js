module.exports = {
  themeConfig: {
    sidebar: [
      '/',
		'/types/',
      '/functions/',
	  '/structures/',
	  '/builtins/',
    ],
	displayAllHeaders: true,
	sidebarDepth: 2,
  },
  markdown: {
    lineNumbers: true
  },
  dest: '../docs/',
  configureWebpack: {
	  output: {
	    publicPath: '/Godel/docs/'
	  }
  }
}  
