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
  base: '/Godel/docs/',
  dest: '../docs/',
  configureWebpack: {
	  output: {
	    publicPath: '/Godel/docs/'
	  }
  }
}  
