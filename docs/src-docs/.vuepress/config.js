module.exports = {
  title: "Gödel",
  themeConfig: {
    sidebar: [
      '/',
	  '/types/',
      '/functions/',
	  '/structures/',
	  '/grammar/',
	  '/architecture/',
    ],
	displayAllHeaders: true,
	sidebarDepth: 2,
  },
  markdown: {
    lineNumbers: true
  },
  head: [
    ['link', { rel: 'icon', href: '/Godel/images/icon.png' }]
  ],
  base: '/Godel/docs/',
  dest: '../docs/',
  configureWebpack: {
    output: {
	  publicPath: '/Godel/docs/'
	}
  }
}  
